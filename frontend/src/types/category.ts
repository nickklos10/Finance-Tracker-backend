export interface Category {
  id?: number;
  name: string;
  description?: string;
}

export interface CategoryListResponse {
  content: Category[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
