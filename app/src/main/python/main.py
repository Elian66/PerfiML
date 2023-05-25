import pandas as pd

def mediaPotencia(arquivo):
    df = pd.read_csv(arquivo)
    df = df.replace('"', '', regex=True)
    return df["battery_power"].mean()

def mediaCpu(arquivo):
    df = pd.read_csv(arquivo)
    df = df.replace('"', '', regex=True)
    return df["cpu_usage"].mean()