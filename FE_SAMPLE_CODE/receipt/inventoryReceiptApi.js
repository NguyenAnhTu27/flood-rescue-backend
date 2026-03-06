// API functions cho phiếu nhập kho
// File này nên được đặt trong: features/relief/api.js hoặc features/inventory/api.js

import httpClient from '../../utils/httpClient'; // Điều chỉnh path theo project của bạn

/**
 * Lấy danh sách phiếu nhập kho
 * @param {Object} params - { page, size, status }
 * @returns {Promise<Object>} - { content: [], totalElements: number, totalPages: number }
 */
export async function listInventoryReceipts(params = {}) {
    try {
        const response = await httpClient.get('/api/inventory/receipts', { params });
        console.log('[API] listInventoryReceipts response:', response);
        
        // BE trả về format: { content: [], totalElements: number, ... }
        if (response.data) {
            return response.data;
        }
        return response;
    } catch (error) {
        console.error('[API] listInventoryReceipts error:', error);
        throw error;
    }
}

/**
 * Lấy chi tiết phiếu nhập kho
 * @param {number} id - ID phiếu nhập
 * @returns {Promise<Object>}
 */
export async function getInventoryReceipt(id) {
    try {
        const response = await httpClient.get(`/api/inventory/receipts/${id}`);
        console.log('[API] getInventoryReceipt response:', response);
        
        if (response.data) {
            return response.data;
        }
        return response;
    } catch (error) {
        console.error('[API] getInventoryReceipt error:', error);
        throw error;
    }
}

/**
 * Duyệt phiếu nhập kho (APPROVE)
 * @param {number} id - ID phiếu nhập
 * @returns {Promise<Object>}
 */
export async function approveInventoryReceipt(id) {
    try {
        console.log('[API] Approving receipt:', id);
        const response = await httpClient.put(`/api/inventory/receipts/${id}/approve`);
        console.log('[API] approveInventoryReceipt response:', response);
        
        if (response.data) {
            return response.data;
        }
        return response;
    } catch (error) {
        console.error('[API] approveInventoryReceipt error:', error);
        
        // Parse error message
        const errorMessage = 
            error?.response?.data?.message || 
            error?.response?.data?.error || 
            error?.message || 
            'Không thể duyệt phiếu nhập';
        
        const customError = new Error(errorMessage);
        customError.data = error?.response?.data;
        customError.status = error?.response?.status;
        throw customError;
    }
}

/**
 * Hủy phiếu nhập kho (CANCEL)
 * @param {number} id - ID phiếu nhập
 * @returns {Promise<Object>}
 */
export async function cancelInventoryReceipt(id) {
    try {
        console.log('[API] Cancelling receipt:', id);
        const response = await httpClient.put(`/api/inventory/receipts/${id}/cancel`);
        console.log('[API] cancelInventoryReceipt response:', response);
        
        if (response.data) {
            return response.data;
        }
        return response;
    } catch (error) {
        console.error('[API] cancelInventoryReceipt error:', error);
        
        // Parse error message
        const errorMessage = 
            error?.response?.data?.message || 
            error?.response?.data?.error || 
            error?.message || 
            'Không thể hủy phiếu nhập';
        
        const customError = new Error(errorMessage);
        customError.data = error?.response?.data;
        customError.status = error?.response?.status;
        throw customError;
    }
}

/**
 * Tạo phiếu nhập kho mới
 * @param {Object} payload - { sourceType, note, lines: [] }
 * @returns {Promise<Object>}
 */
export async function createInventoryReceipt(payload) {
    try {
        const response = await httpClient.post('/api/inventory/receipts', payload);
        console.log('[API] createInventoryReceipt response:', response);
        
        if (response.data) {
            return response.data;
        }
        return response;
    } catch (error) {
        console.error('[API] createInventoryReceipt error:', error);
        throw error;
    }
}
