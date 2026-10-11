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
