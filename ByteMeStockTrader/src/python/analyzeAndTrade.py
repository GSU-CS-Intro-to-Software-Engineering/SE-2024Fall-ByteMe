# analyzeAndTrade.py

import sys
import mysql.connector
import requests
import numpy as np
import pandas as pd
import xgboost as xgb
import joblib

# Explicitly define database configuration
DB_CONFIG = {
    'host': '35.185.41.63',
    'port': 3306,
    'database': 'ByteMeStockTraderDB',
    'user': 'ByteMe_User',
    'password': 'bucgo8-pafkyq-giFqoz'
}

# AlpacaController endpoint
ALPACA_CONTROLLER_URL = "http://localhost:8080/api/execute-trade"

# Load Pre-Trained Model
MODEL_PATH = "Zekrom_Scaled_Tuned.json"
model = xgb.Booster()
model.load_model(MODEL_PATH)

# Load scalers
feature_scaler = joblib.load('feature_scaler.save')
target_scaler = joblib.load('target_scaler.save')

# Rename mapping
FIELD_RENAME_MAPPING = {
    'open_price': 'Open',
    'high_price': 'High',
    'low_price': 'Low',
    'close_price': 'Adj Close',
    'volume': 'Volume',
    'sma': 'SMA_20',
    'ema': 'EMA_20',
    'rsi': 'RSI_14',
    'macd': 'MACD',
    'macd_signal': 'MACD_Signal',
    'middle_band': 'BB_Middle',
    'upper_band': 'BB_Upper',
    'lower_band': 'BB_Lower',
    'obv': 'OBV',
    'atr': 'ATR_14'
}

def normalize_with_saved_scaler(features, expected_features):
    features_df = pd.DataFrame(features, columns=expected_features)
    normalized_features = feature_scaler.transform(features_df)
    return normalized_features

def inverse_transform_predictions(scaled_predictions):
    original_predictions = target_scaler.inverse_transform(scaled_predictions.reshape(-1, 1)).flatten()
    return original_predictions

def fetch_last_n_rows(symbol, n=50):
    """
    Query the SQL database for the last N rows of stock data.
    """
    try:
        connection = mysql.connector.connect(**DB_CONFIG)
        cursor = connection.cursor(dictionary=True)

        query = f"""
        SELECT *
        FROM stock_data_{symbol.lower()}
        ORDER BY date DESC
        LIMIT {n};
        """
        cursor.execute(query)
        results = cursor.fetchall()
        return results[::-1]  # Reverse order to get chronological data
    except mysql.connector.Error as e:
        print(f"Error fetching data for {symbol}: {e}")
        return None
    finally:
        if cursor:
            cursor.close()
        if connection:
            connection.close()

def analyze_stock(data_rows):
    """
    Analyze stock data using the pre-trained XGBoost model to decide Buy, Sell, or Hold.
    """
    # Expected features (ensure the order matches training)
    expected_features = [
        'Open', 'High', 'Low', 'Adj Close', 'Volume',
        'SMA_20', 'EMA_20', 'RSI_14', 'MACD', 'MACD_Signal',
        'BB_Middle', 'BB_Upper', 'BB_Lower', 'OBV', 'ATR_14'
    ]
    print("Expected Feature Names:", expected_features)

    # Extract features from rows
    features = []
    for idx, row in enumerate(data_rows):
        # Rename fields based on the mapping
        renamed_row = {
            FIELD_RENAME_MAPPING[key]: float(value)
            for key, value in row.items()
            if key in FIELD_RENAME_MAPPING
        }
        print(f"Row {idx + 1} Mapped Features:", renamed_row)  # Debug individual row mapping

        # Ensure all expected features are present
        feature_row = [renamed_row.get(feature, 0.0) for feature in expected_features]
        features.append(feature_row)

    # Normalize the features
    features = normalize_with_saved_scaler(features, expected_features)

    # Create DMatrix for prediction
    dmatrix = xgb.DMatrix(features, feature_names=expected_features)

    # Predict using the model
    scaled_predictions = model.predict(dmatrix)

    # Inverse transform the predictions
    predictions = inverse_transform_predictions(scaled_predictions)
    return predictions

def determine_action(predictions, last_close):
    """
    Determine Buy, Sell, or Hold based on predictions.
    """
    avg_predicted_close = (np.mean(predictions) * 3)
    print(f"Average Predicted Close Price: {avg_predicted_close}")
    threshold = 0.02  # 2% price difference for trading decision

    if avg_predicted_close > last_close * (1 + threshold):
        return "buy", int((avg_predicted_close - last_close) * 10)
    elif avg_predicted_close < last_close * (1 - threshold):
        return "sell", int((last_close - avg_predicted_close) * 10)
    else:
        return "hold", 0

def execute_trade(action, quantity, symbol):
    """
    Send trade details to AlpacaController to execute a trade.
    """
    trade_details = {
        "action": action,
        "quantity": quantity,
        "symbol": symbol
    }

    try:
        response = requests.post(ALPACA_CONTROLLER_URL, json=trade_details)
        if response.status_code == 200:
            print(f"Trade executed successfully: {response.json()}")
        else:
            print(f"Failed to execute trade. HTTP {response.status_code}: {response.text}")
    except requests.RequestException as e:
        print(f"Error sending trade request to AlpacaController: {e}")

def main(symbol):
    """
    Main function to fetch stock data, analyze it, and execute trades.
    """
    print(f"Fetching last 50 rows for stock: {symbol}...")
    last_n_data = fetch_last_n_rows(symbol, 50)
    if not last_n_data:
        print("No data available for the stock:", symbol)
        return

    #print("Last Rows of Stock Data:")
    #for row in last_n_data:
    #    print(row)

    # Analyze stock to predict the next close price
    predictions = analyze_stock(last_n_data)
    print(f"Predicted Close Prices: {predictions}")

    # Compare with the most recent close price
    last_close = float(last_n_data[-1].get('close_price', 0))
    print(f"Last Close Price: {last_close}")

    # Determine action based on prediction
    action, quantity = determine_action(predictions, last_close)
    if action == "buy":
        print(f"Executing Buy Order: Buying {quantity} shares of {symbol}.")
        execute_trade("buy", quantity, symbol)
    elif action == "sell":
        print(f"Executing Sell Order: Selling {quantity} shares of {symbol}.")
        execute_trade("sell", quantity, symbol)
    else:
        print("Hold decision. No trade executed.")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Please provide the stock symbol as an argument.")
        sys.exit(1)

    stock_symbol = sys.argv[1]
    main(stock_symbol)