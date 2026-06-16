<template>
  <div class="dashboard-page">
    <h2 class="page-title">数据概览</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card stat-card--today">
          <div class="stat-label">今日订单</div>
          <div class="stat-value">{{ overview.todayCount }}</div>
          <div class="stat-sub">¥{{ fmt(overview.todayAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card stat-card--week">
          <div class="stat-label">本周订单</div>
          <div class="stat-value">{{ overview.weekCount }}</div>
          <div class="stat-sub">¥{{ fmt(overview.weekAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card stat-card--month">
          <div class="stat-label">本月订单</div>
          <div class="stat-value">{{ overview.monthCount }}</div>
          <div class="stat-sub">¥{{ fmt(overview.monthAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card stat-card--pending">
          <div class="stat-label">待处理</div>
          <div class="stat-value">{{ overview.pendingCount }}</div>
          <div class="stat-sub">笔订单</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势 + 热销 -->
    <el-row :gutter="16" class="content-row">
      <el-col :span="16">
        <el-card shadow="never">
          <template #header><span class="card-header-title">近7天订单趋势</span></template>
          <div class="trend-chart" v-if="trend.length">
            <div class="trend-bar-row">
              <div
                v-for="(item, i) in trend"
                :key="i"
                class="trend-bar-col"
              >
                <div class="trend-bar-wrapper">
                  <div
                    class="trend-bar"
                    :style="{ height: barHeight(item.count, maxTrendCount) + 'px' }"
                    :title="item.date + ': ' + item.count + '单'"
                  ></div>
                </div>
                <div class="trend-bar-label">{{ fmtDate(item.date) }}</div>
                <div class="trend-bar-value">{{ item.count }}单</div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无数据" :image-size="80" />
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never">
          <template #header><span class="card-header-title">热销菜品 Top10</span></template>
          <div v-if="topDishes.length" class="top-list">
            <div
              v-for="(item, i) in topDishes"
              :key="item.dishId"
              class="top-item"
            >
              <span class="top-rank" :class="'rank-' + (i + 1)">{{ i + 1 }}</span>
              <span class="top-name">{{ item.dishName }}</span>
              <span class="top-qty">{{ item.totalQty }}份</span>
            </div>
          </div>
          <el-empty v-else description="暂无数据" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getOverview, getTrend, getTopDishes } from '@/api/statistics'
import type { Overview, TrendItem, TopDish } from '@/api/statistics'

const overview = ref<Overview>({
  todayCount: 0, todayAmount: 0,
  weekCount: 0, weekAmount: 0,
  monthCount: 0, monthAmount: 0,
  pendingCount: 0
})
const trend = ref<TrendItem[]>([])
const topDishes = ref<TopDish[]>([])
const loading = ref(true)

const maxTrendCount = computed(() => {
  if (!trend.value.length) return 1
  return Math.max(...trend.value.map(t => t.count), 1)
})

const barHeight = (count: number, max: number) => {
  return Math.max((count / max) * 120, count > 0 ? 4 : 0)
}

const fmt = (v: unknown) => (Number(v) || 0).toFixed(2)
const fmtDate = (d: string) => {
  const parts = d.split('-')
  return parts.length === 3 ? parts[1] + '/' + parts[2] : d
}

onMounted(async () => {
  try {
    const [ov, tr, top] = await Promise.all([
      getOverview(),
      getTrend(7),
      getTopDishes(10)
    ])
    overview.value = ov
    trend.value = tr
    topDishes.value = top
  } catch {
    // interceptor handles
  } finally {
    loading.value = false
  }
})
</script>

<style scoped lang="scss">
.dashboard-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 22px;
  color: #303133;
  margin-bottom: 20px;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  border-radius: 8px;

  .stat-label { font-size: 13px; color: #909399; margin-bottom: 8px; }
  .stat-value { font-size: 32px; font-weight: 700; color: #303133; }
  .stat-sub  { font-size: 13px; color: #909399; margin-top: 4px; }

  &.stat-card--today   { border-top: 3px solid #409eff; }
  &.stat-card--week    { border-top: 3px solid #67c23a; }
  &.stat-card--month   { border-top: 3px solid #e6a23c; }
  &.stat-card--pending { border-top: 3px solid #f56c6c; }
}

.content-row {
  .el-card { border-radius: 8px; }
}

.card-header-title {
  font-weight: 600;
  font-size: 15px;
  color: #303133;
}

/* 柱状图 */
.trend-chart {
  padding: 8px 0 16px;
}

.trend-bar-row {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  justify-content: space-around;
  min-height: 180px;
}

.trend-bar-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.trend-bar-wrapper {
  display: flex;
  align-items: flex-end;
  height: 130px;
}

.trend-bar {
  width: 32px;
  background: linear-gradient(180deg, #409eff, #79bbff);
  border-radius: 4px 4px 0 0;
  min-height: 4px;
  transition: height 0.4s ease;
}

.trend-bar-label {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}

.trend-bar-value {
  font-size: 12px;
  color: #303133;
  font-weight: 500;
}

/* 热销榜 */
.top-list {
  .top-item {
    display: flex;
    align-items: center;
    padding: 8px 0;
    border-bottom: 1px solid #f2f3f5;

    &:last-child { border-bottom: none; }
  }

  .top-rank {
    width: 24px;
    height: 24px;
    border-radius: 4px;
    background: #dcdfe6;
    color: #606266;
    font-size: 12px;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 10px;
    flex-shrink: 0;

    &.rank-1 { background: #f56c6c; color: #fff; }
    &.rank-2 { background: #e6a23c; color: #fff; }
    &.rank-3 { background: #409eff; color: #fff; }
  }

  .top-name {
    flex: 1;
    font-size: 14px;
    color: #303133;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .top-qty {
    font-size: 13px;
    color: #909399;
    flex-shrink: 0;
  }
}
</style>
