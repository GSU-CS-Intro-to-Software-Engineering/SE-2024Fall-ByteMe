# ByteMe

Automatic stock trader

Team Lead: Daniel Troyano, dt09800@georgiasouthern.edu

Team Member 1: Corbin Beal, cb40329@georgiasouthern.edu

Team Member 2: Atenea Caldera, ac34318@georgiasouthern.edu

## Prerequisites

Python3 globally installed on your machine
pip (Python package manager)

## Running the Project

1. Clone the repository: git clone [https://github.com/DanielTroyano/ByteMeStockTrader.git](https://github.com/GSU-CS-Intro-to-Software-Engineering/SE-2024Fall-ByteMe.git)

2. Install java dependancies using Maven: 'mvn clean install'

3. Create a virtual python env: 'python3 -m venv venv'

4. Set the virtual env: 'source venv/bin/activate'

5. Install sentiment analysis AI: 'pip install transformers torch'

6. Make account on rapidapi.com

7. Obtain YAHOO FINANCE API DATA api key

8. If on mac: Enter terminal and load API key into '~/.zshrc' as 'YAHOO_FINANCE_API_KEY' and save using 'source ~/.zshrc' || If on mac, add API key to user env variables under 'YAHOO_FINANCE_API_KEY'

9. Run Backend: (1) cd into /ByteMeStockTrader (2) Press play in the top right of Main.js

10. Run Frontend: (1) cd into /ByteMeStockTrader (2) Use "mvn spring-boot:run" in terminal (3) Open localhost:8080 in browser
