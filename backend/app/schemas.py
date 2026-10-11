"""请求/响应数据模型"""
from typing import List, Optional

from pydantic import BaseModel, Field


class LoginRequest(BaseModel):
    phone: str = Field(..., min_length=11, max_length=11)
    password: str = Field(..., min_length=6)


class RegisterRequest(BaseModel):
    phone: str = Field(..., min_length=11, max_length=11)
    password: str = Field(..., min_length=6)
    nickname: str = Field(..., min_length=1, max_length=20)


class UpdateProfileRequest(BaseModel):
    nickname: Optional[str] = None
    avatar: Optional[str] = None


class OrderItemIn(BaseModel):
    category_id: str
    estimated_weight: float = Field(..., gt=0)


class CreateOrderRequest(BaseModel):
    user_id: str
    address_id: str
    appointment_time: str
    items: List[OrderItemIn]
    remark: Optional[str] = ""


class AddressIn(BaseModel):
    user_id: str
    contact_name: str
    phone: str
    region: str
    detail: str
    is_default: bool = False


class RedeemRequest(BaseModel):
    user_id: str
    product_id: str
    quantity: int = Field(1, ge=1, le=10)


class SignInRequest(BaseModel):
    user_id: str


# ---------- 回收员端 ----------
class RecyclerActionRequest(BaseModel):
    recycler_id: str


class CompleteItemIn(BaseModel):
    category_id: str
    actual_weight: float = Field(..., ge=0)


class CompleteOrderRequest(BaseModel):
    recycler_id: str
    items: List[CompleteItemIn]


# ---------- 管理后台 ----------
class UpdateCategoryRequest(BaseModel):
    price_per_kg: Optional[float] = None
    points_per_kg: Optional[int] = None
    carbon_per_kg: Optional[float] = None
    description: Optional[str] = None


class ProductIn(BaseModel):
    name: str
    description: str
    image: str
    points: int = Field(..., ge=1)
    stock: int = Field(..., ge=0)
    category: str
    is_hot: bool = False


class UpdateProductRequest(BaseModel):
    name: Optional[str] = None
    description: Optional[str] = None
    image: Optional[str] = None
    points: Optional[int] = None
    stock: Optional[int] = None
    category: Optional[str] = None
    is_hot: Optional[bool] = None


class ArticleIn(BaseModel):
    title: str
    summary: str
    content: str
    cover: str
    category: str
    author: str = "绿循环官方"


class UpdateUserStatusRequest(BaseModel):
    status: Optional[str] = None  # active / disabled
    role: Optional[str] = None  # user / recycler / admin


class UpdateRedemptionStatusRequest(BaseModel):
    status: str  # pending / shipped / completed


class AssignOrderRequest(BaseModel):
    recycler_id: str


# ---------- 位置 / 通知 / 钱包 ----------
class LocationUpdateRequest(BaseModel):
    recycler_id: str
    lat: float
    lng: float
    heading: Optional[float] = None
    speed: Optional[float] = None


class AddressGeoIn(AddressIn):
    lat: Optional[float] = None
    lng: Optional[float] = None


class MarkReadRequest(BaseModel):
    user_id: str
    ids: Optional[List[str]] = None  # 为空则全部已读


class PayOrderRequest(BaseModel):
    recycler_id: str
    pay_method: str  # cash / wechat / alipay


class WithdrawRequest(BaseModel):
    recycler_id: str
    amount: float = Field(..., gt=0)
    account: str


class ReviewWithdrawRequest(BaseModel):
    approve: bool
    remark: Optional[str] = ""
