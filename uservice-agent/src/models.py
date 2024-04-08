from dataclasses import dataclass


@dataclass(frozen=True)
class MyState:
  budget: float = 0
  owned: dict[str, int] = {}

@dataclass(frozen=True)
class ChangeBudget:
  delta: float

@dataclass(frozen=True)
class BuyOrder:
  """Ticker"""
  ticker: str
  """Price which should be used to by the asset"""
  price: float
