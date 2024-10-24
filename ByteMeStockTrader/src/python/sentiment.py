from transformers import BertTokenizer, BertForSequenceClassification
from transformers import pipeline
import torch

# Use GPU if possible
device = 0 if torch.cuda.is_available() else -1

if(torch.cuda.is_available()):
    device = 0
    print("-FinBert analyzing article title sentiment using GPU . . .\n")
else:
    device = -1
    print("-FinBert analyzing article title sentiment using CPU . . .\n")

# TODO 1: further train the model using custom dataset and trainer.train()
# TODO 2: expand lexicon with more financial terms
# TODO 3: better handle context understanding by identifying words like "but" and "however" / change in tone
# TODO 4: find way to better understand numerical data in titles
# TODO 5: offload to cloud? (Google Colab)

# Load FinBERT pre-trained model (finbert-tone ideal for financial jargon)
tokenizer = BertTokenizer.from_pretrained('yiyanghkust/finbert-tone')
model = BertForSequenceClassification.from_pretrained('yiyanghkust/finbert-tone')

# Initialize pipeline
nlp = pipeline("sentiment-analysis", model=model, tokenizer=tokenizer, device=device, max_length=200, batch_size=10)

# Batch processing function
def analyze_sentiment_batch(titles):
    result = nlp(titles)
    return result

if __name__ == "__main__":
    import sys

    # Sentiment counters
    positive_count = 0
    negative_count = 0
    neutral_count = 0

    titles = [line.strip() for line in sys.stdin if line.strip()]  # Collect all titles from input
    if titles:
        # Analyze sentiment for the batch of titles
        sentiments = analyze_sentiment_batch(titles)
        for title, sentiment in zip(titles, sentiments):
            print(f"Title: {title}")
            print(f"Sentiment: {sentiment}")

            # Increment counters based on sentiment label
            if sentiment['label'] == 'Positive':
                positive_count += 1
            elif sentiment['label'] == 'Negative':
                negative_count += 1
            elif sentiment['label'] == 'Neutral':
                neutral_count += 1

        # Print out the counts
        print("\nSentiment Summary:")
        print(f"Positive Count: {positive_count}")
        print(f"Negative Count: {negative_count}")
        print(f"Neutral Count: {neutral_count}\n")

# TODO 6: Store sentiment data in database