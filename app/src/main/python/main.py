import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor
import matplotlib.pyplot as plt

def getFeatureInfluenceInBatteryPower(file):
    dataset = pd.read_csv(file)
    dataset = dataset.dropna()
    dataset = dataset[["battery_power","screen_status", "bright_level", "bright_mode", "screen_on_time", "bluetooth", "gps_status", "gps_activity", "nfc", "flashlight", "airplane_mode", "fingerprint", "orientation", "battery_level", "battery_health", "battery_charging_status", "battery_connection_status", "battery_temperature", "battery_voltage", "network_mode", "mobile_mode", "mobile_status", "mobile_roaming", "mobile_rx", "mobile_tx", "wifi_status", "wifi_intensity", "wifi_speed", "wifi_ap", "wifi_rx", "wifi_tx", "mcc", "mnc", "ring_mode", "sound_level", "playback_status", "ram_usage", "ram_free", "rom_usage", "rom_free", "cpu_usage", "cpu_temperature", "up_time", "sleep_time", "frequency_core0", "frequency_core1", "frequency_core2", "frequency_core3", "frequency_core4", "frequency_core5", "frequency_core6", "frequency_core7", "foreground_app"]]
    df = dataset.sample(frac = 0.2)
    ref = df.columns
    categorical = []
    poor_categorical = []
    numerical = []

    for k in ref:
        if len(df[k].value_counts()) > 1 and len(df[k].value_counts()) < 10:
            categorical.append(k)
        if len(df[k].value_counts()) == 1:
            poor_categorical.append(k)
        if len(df[k].value_counts()) >  10:
            numerical.append(k)

    for k in categorical:
        df[k] = df[k].apply(lambda x:str(x))

    for k in numerical:
        df[k] = df[k].apply(lambda x:int(x))


    X = df[numerical+categorical]
    y = df["battery_power"]

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
    rf = RandomForestRegressor(n_estimators=100, random_state=42)

    # Criando as colunas dummy (variáveis categóricas)

    X_train = pd.get_dummies(X_train, drop_first=True)
    X_test = pd.get_dummies(X_test, drop_first=True)

    rf.fit(X_train, y_train)

    importances = pd.DataFrame({'feature': X_train.columns, 'importance': rf.feature_importances_})
    importances = importances.sort_values('importance', ascending=False)
    importances.reset_index(drop= 'index', inplace = True)
    importances_str = importances.to_string(index=False)

    return importances_str

def getNullColumnsByDataset(file):
    df = pd.read_csv(file)
    string = ""
    for dvc in df['device_id'].unique():
        if not pd.isnull(dvc):
            df_dvc = df.loc[df['device_id'] == dvc]
            nan_cols = df_dvc.columns[df_dvc.isnull().any()]

            if(len(nan_cols) > 0):
                string += "The following columns below presents NaN:\n  "
                for col in nan_cols:
                    string += col + ",\n  "
            else:
                string +=  "The device" + " " + str(dvc) + " " + "doesn't shows missing values.\n  "
        else:
            string += "Was detected a null device id in the collect.\n  "
        string = string[:-2]
    return string

def getDeviceCollectPercentageInGeneral(file):
    df = pd.read_csv(file)
    df["_timestamp"] = pd.to_datetime(df["_timestamp"])

    df.sort_values(by = '_timestamp', inplace = True)

    df['gap'] = df['_timestamp'].diff().dt.total_seconds()

    df['date'] = df['_timestamp'].dt.date

    len_devices = []
    for dvc in df.device_id.unique():
        len_devices.append((dvc ,len(df.loc[df.device_id == dvc])))

    len_devices = sorted(len_devices, key = lambda x: x[1])

    data = [{'device_id': dvc, 'collected': str(round(100 * collected / df.shape[0], 2)) + "%"}
            for dvc, collected in len_devices]
    df2 = pd.DataFrame(data)
    df2.reset_index(drop= 'index', inplace = True)
    df2_str = df2.to_string(index=False)

    return df2_str