from fastapi import FastAPI
from typing import Optional
from pydantic import BaseModel

app = FastAPI()

# Pydantic model for request body validation
class Item(BaseModel):
    name: str
    price: float
    is_offer: Optional[bool] = None

@app.get("/")
def read_root():
    """Root endpoint returning a simple message."""
    return {"Hello": "World"}

@app.get("/items/{item_id}")
def read_item(item_id: int, q: Optional[str] = None):
    """Endpoint with a path and query parameter."""
    return {"item_id": item_id, "q": q}

@app.put("/items/{item_id}")
def update_item(item_id: int, item: Item):
    """Endpoint demonstrating request body and type validation."""
    return {"item_name": item.name, "item_id": item_id, "is_offer": item.is_offer}