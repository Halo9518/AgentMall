<template>
  <div class="dishes-page">
    <div class="page-header">
      <h2>菜品管理</h2>
      <el-button type="primary" @click="showAddDialog">添加菜品</el-button>
    </div>

    <!-- 筛选栏 -->
    <el-card class="filter-bar" shadow="never">
      <el-select
        v-model="filterCategoryId"
        placeholder="全部分类"
        clearable
        @change="onFilterChange"
        style="width: 180px"
      >
        <el-option
          v-for="cat in categories"
          :key="cat.id"
          :label="cat.name"
          :value="cat.id"
        />
      </el-select>
    </el-card>

    <!-- 表格 -->
    <el-card>
      <el-table :data="dishes" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="菜品名称" min-width="120" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="salesCount" label="销量" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showEditDialog(row)">编辑</el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              link
              @click="handleToggle(row)"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="onFilterChange"
          @current-change="onFilterChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑菜品' : '添加菜品'"
      width="480px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="所属分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="菜品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜品名称" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="图片URL">
          <el-input v-model="form.image" placeholder="可选，输入图片链接" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="可选，菜品描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getDishes,
  createDish,
  updateDish,
  toggleDishStatus,
  deleteDish,
  getCategories
} from '@/api/dish'
import type { DishVO, Category } from '@/api/dish'

const dishes = ref<DishVO[]>([])
const categories = ref<Category[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterCategoryId = ref<number | undefined>()

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(0)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  categoryId: undefined as number | undefined,
  name: '',
  price: 0,
  image: '',
  description: ''
})

const rules: FormRules = {
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getDishes({
      page: page.value,
      pageSize: pageSize.value,
      categoryId: filterCategoryId.value
    })
    dishes.value = res.records
    total.value = res.total
  } catch {
    // interceptor handles
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    categories.value = await getCategories()
  } catch {
    // interceptor handles
  }
}

const onFilterChange = () => {
  page.value = 1
  fetchData()
}

const showAddDialog = () => {
  isEdit.value = false
  form.categoryId = undefined
  form.name = ''
  form.price = 0.01
  form.image = ''
  form.description = ''
  dialogVisible.value = true
}

const showEditDialog = (row: DishVO) => {
  isEdit.value = true
  editingId.value = row.id
  form.categoryId = row.categoryId
  form.name = row.name
  form.price = row.price
  form.image = row.image || ''
  form.description = row.description || ''
  dialogVisible.value = true
}

const resetForm = () => {
  formRef.value?.resetFields()
}

const onSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = {
      categoryId: form.categoryId!,
      name: form.name,
      price: form.price,
      image: form.image || undefined,
      description: form.description || undefined
    }
    if (isEdit.value) {
      await updateDish(editingId.value, data)
      ElMessage.success('修改成功')
    } else {
      await createDish(data)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    await fetchData()
  } catch {
    // interceptor handles
  } finally {
    submitting.value = false
  }
}

const handleToggle = async (row: DishVO) => {
  try {
    await toggleDishStatus(row.id)
    ElMessage.success(row.status === 1 ? '已下架' : '已上架')
    await fetchData()
  } catch {
    // interceptor handles
  }
}

const handleDelete = async (row: DishVO) => {
  try {
    await ElMessageBox.confirm(`确定删除菜品「${row.name}」？`, '提示', {
      type: 'warning'
    })
    await deleteDish(row.id)
    ElMessage.success('删除成功')
    await fetchData()
  } catch {
    // cancelled or error
  }
}

onMounted(() => {
  fetchCategories()
  fetchData()
})
</script>

<style scoped lang="scss">
.dishes-page {
  padding: 20px;
  max-width: 1100px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h2 {
    font-size: 20px;
    color: #303133;
  }
}

.filter-bar {
  margin-bottom: 16px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
