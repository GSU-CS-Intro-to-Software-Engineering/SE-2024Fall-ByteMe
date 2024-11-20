import mysql.connector
import requests

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

def fetch_latest_data(symbol):
    """
    Query the SQL database for the latest stock data.
    """
    try:
        connection = mysql.connector.connect(**DB_CONFIG)
        cursor = connection.cursor(dictionary=True)

        query = f"""
        SELECT *
        FROM stock_data_{symbol.lower()}
        ORDER BY date DESC
        LIMIT 1;
        """
        cursor.execute(query)
        result = cursor.fetchone()
        return result
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
            # Safely parse JSON if available
            try:
                json_response = response.json()
                print("Trade executed successfully:", json_response)
            except ValueError:
                print("Trade executed successfully, but response is not JSON:", response.text)
        else:
            print(f"Failed to execute trade. HTTP {response.status_code}: {response.text}")
    except requests.RequestException as e:
        print(f"Error sending trade request to AlpacaController: {e}")

def main(symbol):
    """
    Main function to fetch stock data, analyze it, and execute trades based on logic.
    """
    print(f"Analyzing stock: {symbol}...")

    stock_data = fetch_latest_data(symbol)
    if not stock_data:
        print("No data available for the stock:", symbol)
        return

    print("Latest Stock Data:", stock_data)

    # Temporary logic to force a trade for testing purposes
    label = "Buy"  # Force a "Buy" decision
    confidence = 0.8  # Set a high confidence score for testing

    print(f"Decision: {label} with confidence {confidence}")

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
    # Replace the below placeholder with the stock symbol you want to test
    symbol = "NVDA"
    main(symbol)
