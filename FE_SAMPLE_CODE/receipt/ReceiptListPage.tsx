// Component danh sách phiếu nhập kho với chức năng duyệt
import React, { useState, useEffect } from 'react';
import { 
  getReceipts, 
  approveReceipt, 
  cancelReceipt,
  type InventoryReceiptResponse 
} from './receiptApi';
import { Button, Table, Tag, Modal, message, Space, Select } from 'antd';
import type { ColumnsType } from 'antd/es/table';

const { Option } = Select;

const ReceiptListPage: React.FC = () => {
  const [receipts, setReceipts] = useState<InventoryReceiptResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [statusFilter, setStatusFilter] = useState<'DRAFT' | 'APPROVED' | 'DONE' | 'CANCELLED' | undefined>(undefined);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 20,
    total: 0,
  });

  const fetchReceipts = async () => {
    setLoading(true);
    try {
      const response = await getReceipts({
        status: statusFilter,
        page: pagination.current - 1,
        size: pagination.pageSize,
      });
      setReceipts(response.content);
      setPagination(prev => ({
        ...prev,
        total: response.totalElements,
      }));
    } catch (error: any) {
      message.error('Lỗi khi tải danh sách phiếu nhập: ' + (error?.message || 'Unknown error'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReceipts();
  }, [statusFilter, pagination.current]);

  const handleApprove = async (id: number, code: string) => {
    Modal.confirm({
      title: 'Xác nhận duyệt phiếu nhập',
      content: `Bạn có chắc chắn muốn duyệt phiếu nhập "${code}"? Sau khi duyệt, tồn kho sẽ được cập nhật.`,
      onOk: async () => {
        try {
          await approveReceipt(id);
          message.success(`Đã duyệt phiếu nhập ${code} thành công!`);
          fetchReceipts();
        } catch (error: any) {
          message.error('Lỗi khi duyệt phiếu nhập: ' + (error?.response?.data?.message || error?.message || 'Unknown error'));
        }
      },
    });
  };

  const handleCancel = async (id: number, code: string) => {
    Modal.confirm({
      title: 'Xác nhận hủy phiếu nhập',
      content: `Bạn có chắc chắn muốn hủy phiếu nhập "${code}"?`,
      okText: 'Hủy phiếu',
      okButtonProps: { danger: true },
      onOk: async () => {
        try {
          await cancelReceipt(id);
          message.success(`Đã hủy phiếu nhập ${code} thành công!`);
          fetchReceipts();
        } catch (error: any) {
          message.error('Lỗi khi hủy phiếu nhập: ' + (error?.response?.data?.message || error?.message || 'Unknown error'));
        }
      },
    });
  };

  const getStatusTag = (status: string) => {
    const statusMap: Record<string, { color: string; text: string }> = {
      DRAFT: { color: 'default', text: 'Nháp' },
      APPROVED: { color: 'processing', text: 'Đã duyệt' },
      DONE: { color: 'success', text: 'Hoàn thành' },
      CANCELLED: { color: 'error', text: 'Đã hủy' },
    };
    const config = statusMap[status] || { color: 'default', text: status };
    return <Tag color={config.color}>{config.text}</Tag>;
  };

  const getSourceTypeTag = (sourceType: string) => {
    return sourceType === 'DONATION' ? (
      <Tag color="blue">Quyên góp</Tag>
    ) : (
      <Tag color="green">Mua vào</Tag>
    );
  };

  const columns: ColumnsType<InventoryReceiptResponse> = [
    {
      title: 'Mã phiếu',
      dataIndex: 'code',
      key: 'code',
      width: 150,
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status) => getStatusTag(status),
    },
    {
      title: 'Nguồn hàng',
      dataIndex: 'sourceType',
      key: 'sourceType',
      width: 120,
      render: (sourceType) => getSourceTypeTag(sourceType),
    },
    {
      title: 'Số dòng',
      key: 'lineCount',
      width: 100,
      render: (_, record) => record.lines?.length || 0,
    },
    {
      title: 'Ghi chú',
      dataIndex: 'note',
      key: 'note',
      ellipsis: true,
    },
    {
      title: 'Ngày tạo',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (date) => date ? new Date(date).toLocaleString('vi-VN') : '-',
    },
    {
      title: 'Thao tác',
      key: 'action',
      width: 200,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          {record.status === 'DRAFT' && (
            <>
              <Button
                type="primary"
                size="small"
                onClick={() => handleApprove(record.id, record.code)}
              >
                Duyệt
              </Button>
              <Button
                danger
                size="small"
                onClick={() => handleCancel(record.id, record.code)}
              >
                Hủy
              </Button>
            </>
          )}
          <Button size="small" onClick={() => {
            // Xem chi tiết - bạn có thể mở modal hoặc navigate
            Modal.info({
              title: `Chi tiết phiếu nhập ${record.code}`,
              width: 800,
              content: (
                <div>
                  <p><strong>Mã phiếu:</strong> {record.code}</p>
                  <p><strong>Trạng thái:</strong> {getStatusTag(record.status)}</p>
                  <p><strong>Nguồn hàng:</strong> {getSourceTypeTag(record.sourceType)}</p>
                  <p><strong>Ghi chú:</strong> {record.note || '-'}</p>
                  <Table
                    dataSource={record.lines}
                    columns={[
                      { title: 'Mặt hàng', dataIndex: 'itemCategoryName', key: 'name' },
                      { title: 'Số lượng', dataIndex: 'qty', key: 'qty' },
                      { title: 'Đơn vị', dataIndex: 'unit', key: 'unit' },
                    ]}
                    pagination={false}
                    size="small"
                  />
                </div>
              ),
            });
          }}>
            Chi tiết
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: '24px' }}>
      <div style={{ marginBottom: '16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>Danh sách phiếu nhập kho</h2>
        <Select
          style={{ width: 200 }}
          placeholder="Lọc theo trạng thái"
          allowClear
          value={statusFilter}
          onChange={(value) => {
            setStatusFilter(value);
            setPagination(prev => ({ ...prev, current: 1 }));
          }}
        >
          <Option value="DRAFT">Nháp</Option>
          <Option value="APPROVED">Đã duyệt</Option>
          <Option value="DONE">Hoàn thành</Option>
          <Option value="CANCELLED">Đã hủy</Option>
        </Select>
      </div>

      <Table
        columns={columns}
        dataSource={receipts}
        rowKey="id"
        loading={loading}
        pagination={{
          ...pagination,
          showSizeChanger: true,
          showTotal: (total) => `Tổng ${total} phiếu nhập`,
          onChange: (page, pageSize) => {
            setPagination(prev => ({
              ...prev,
              current: page,
              pageSize: pageSize || 20,
            }));
          },
        }}
        scroll={{ x: 1200 }}
      />
    </div>
  );
};

export default ReceiptListPage;
