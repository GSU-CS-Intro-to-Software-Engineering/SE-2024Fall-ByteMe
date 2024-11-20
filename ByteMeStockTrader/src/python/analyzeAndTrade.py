import mysql.connector
import requests
import pickle
import numpy as np

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
MODEL_PATH = "Zekrom_model.json"
with open(MODEL_PATH, "rb") as f:
    model = pickle.load(f)

def fetch_last_10_rows(symbol):
    """
    Query the SQL database for the last 10 rows of stock data.
    """
    try:
        connection = mysql.connector.connect(**DB_CONFIG)
        cursor = connection.cursor(dictionary=True)

        query = f"""
        SELECT *
        FROM stock_data_{symbol.lower()}
        ORDER BY date DESC
        LIMIT 10;
        """
        cursor.execute(query)
        results = cursor.fetchall()
        return results
    except mysql.connector.Error as e:
        print(f"Error fetching data for {symbol}: {e}")
    finally:
        if cursor:
            cursor.close()
        if connection:
            connection.close()

def execute_trade(action, quantity):
    """
    Send trade details to AlpacaController to execute a trade.
    """
    trade_details = {
        "action": action,
        "quantity": quantity
    }

    try:
        response = requests.post(ALPACA_CONTROLLER_URL, json=trade_details)
        print(f"Response Status Code: {response.status_code}")
        print(f"Response Content: {response.text}")  # Print full response for debugging

        if response.status_code == 200:
            print("Trade executed successfully:", response.json())
        else:
            print(f"Failed to execute trade. HTTP {response.status_code}: {response.text}")
    except requests.RequestException as e:
        print(f"Error sending trade request to AlpacaController: {e}")

def analyze_stock(data_rows):
    """
    Analyze stock data using a pre-trained AI model to decide whether to Buy, Sell, or Hold.
    """
    # Extract features from rows
    features = []
    for row in data_rows:
        features.append([
            float(row['sma']),
            float(row['ema']),
            float(row['rsi']),
            float(row['macd']),
            float(row['macd_signal']),
            float(row['macd_hist']),
            float(row['upper_band']),
            float(row['middle_band']),
            float(row['lower_band']),
            float(row['obv']),
            float(row['atr']),
        ])

    # Convert features into a NumPy array
    features = np.array(features)

    # Average the features (or apply other aggregation as needed)
    avg_features = features.mean(axis=0).reshape(1, -1)

    # Predict action using the pre-trained model
    prediction = model.predict(avg_features)[0]  # 'Buy', 'Sell', 'Hold'
    confidence = max(model.predict_proba(avg_features)[0])  # Confidence score
    return prediction, confidence

def main(symbol):
    """
    Main function to fetch stock data, analyze it, and execute trades based on logic.
    """
    print(f"Fetching last 10 rows for stock: {symbol}...")

    last_10_data = fetch_last_10_rows(symbol)
    if not last_10_data:
        print("No data available for the stock:", symbol)
        return

    print("Last 10 Rows of Stock Data:")
    for row in last_10_data:
        print(row)

    # Analyze stock using the AI model
    label, confidence = analyze_stock(last_10_data)
    print(f"Model Decision: {label} with confidence {confidence:.2f}")

    if label == "Buy":
        quantity = max(int(confidence * 10), 1)  # Adjust quantity based on confidence
        print(f"Executing Buy Order: {quantity} shares of {symbol}")
        execute_trade("buy", quantity)
    elif label == "Sell":
        quantity = max(int(confidence * 10), 1)
        print(f"Executing Sell Order: {quantity} shares of {symbol}")
        execute_trade("sell", quantity)
    else:
        print("Hold decision. No trade executed.")

if __name__ == "__main__":
    symbol = "NVDA"
    main(symbol)
