// API functions cho phiếu nhập kho
import httpClient from '@/utils/httpClient'; // Điều chỉnh path theo project của bạn

export interface InventoryReceiptLineRequest {
  itemCategoryId: number;
  qty: number;
  unit: string;
}

export interface InventoryReceiptCreateRequest {
  sourceType: 'DONATION' | 'PURCHASE';
  note?: string;
  lines: InventoryReceiptLineRequest[];
}

export interface InventoryReceiptLineResponse {
  itemCategoryId: number;
  itemCategoryName: string;
  qty: number;
  unit: string;
}

export interface InventoryReceiptResponse {
  id: number;
  code: string;
  status: 'DRAFT' | 'APPROVED' | 'DONE' | 'CANCELLED';
  sourceType: 'DONATION' | 'PURCHASE';
  createdById: number;
  note?: string;
  createdAt: string;
  updatedAt?: string;
  lines: InventoryReceiptLineResponse[];
}

export interface ReceiptListResponse {
  content: InventoryReceiptResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// Tạo phiếu nhập kho
export async function createReceipt(
  payload: InventoryReceiptCreateRequest
): Promise<InventoryReceiptResponse> {
  const res = await httpClient.post<InventoryReceiptResponse>(
    '/api/inventory/receipts',
    payload
  );
  return res.data;
}

// Lấy danh sách phiếu nhập kho
export async function getReceipts(params?: {
  status?: 'DRAFT' | 'APPROVED' | 'DONE' | 'CANCELLED';
  page?: number;
  size?: number;
}): Promise<ReceiptListResponse> {
  const res = await httpClient.get<ReceiptListResponse>(
    '/api/inventory/receipts',
    { params }
  );
  return res.data;
}

// Lấy chi tiết phiếu nhập kho
export async function getReceipt(id: number): Promise<InventoryReceiptResponse> {
  const res = await httpClient.get<InventoryReceiptResponse>(
    `/api/inventory/receipts/${id}`
  );
  return res.data;
}

// Duyệt phiếu nhập kho
export async function approveReceipt(id: number): Promise<InventoryReceiptResponse> {
  const res = await httpClient.put<InventoryReceiptResponse>(
    `/api/inventory/receipts/${id}/approve`
  );
  return res.data;
}

// Hủy phiếu nhập kho
export async function cancelReceipt(id: number): Promise<InventoryReceiptResponse> {
  const res = await httpClient.put<InventoryReceiptResponse>(
    `/api/inventory/receipts/${id}/cancel`
  );
  return res.data;
}
