import nltk

# Download VADER lexicon
nltk.download('vader_lexicon')

from nltk.sentiment.vader import SentimentIntensityAnalyzer

# Initialize VADER
analyzer = SentimentIntensityAnalyzer()

def analyze_sentiment(text):
    sentiment = analyzer.polarity_scores(text)
    return sentiment['compound']  # Compound score is what we're interested in

if __name__ == "__main__":
    import sys
    for line in sys.stdin:
        print(f"Title: {line.strip()}")
        score = analyze_sentiment(line.strip()) * 10
        print(f"Sentiment Score: {score}")
        
# echo "I am so super happy!!!" | python3 sentiment.py
