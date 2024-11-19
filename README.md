# ByteMe

Automatic stock trader

Team Lead: Daniel Troyano, dt09800@georgiasouthern.edu

Team Member 1: Corbin Beal, cb40329@georgiasouthern.edu

Team Member 2: Atenea Caldera, ac34318@georgiasouthern.edu

## Prerequisites

-Python3 globally installed on your machine
-pip (Python package manager)

## PROJECT SETUP

1. Clone the repository: git clone [https://github.com/DanielTroyano/ByteMeStockTrader.git](https://github.com/GSU-CS-Intro-to-Software-Engineering/SE-2024Fall-ByteMe.git)

2. Install java dependancies using Maven: 'mvn clean install'

3. Create a virtual python env: 'python3 -m venv venv'

4. Set the virtual env: 'source venv/bin/activate'

5. Install sentiment analysis AI: 'pip install transformers torch'

6. Make account on rapidapi.com -> obtain YAHOO FINANCE API DATA api key

7. If on mac: Enter terminal and load API key into '~/.zshrc' as 'YAHOO_FINANCE_API_KEY' and save using 'source ~/.zshrc' || If on windows, add API key to user env variables under 'YAHOO_FINANCE_API_KEY'

8. Make account on twelvedata.com -> obtain API key

9. If on mac: Enter terminal and load API key into '~/.zshrc' as 'TWELVE_DATA_API_KEY' and save using 'source ~/.zshrc' || If on windows, add API key to user env variables under 'TWELVE_DATA_API_KEY'

## RUNNING PROJECT

1. Run Frontend connected with Backend: (1) cd into /ByteMeStockTrader (Run "mvn clean install again to be safe) (2) Run "mvn spring-boot:run" (3) Open localhost:8080 in browser

## ERRORS

1. If you get this error: "Error: LinkageError occurred while loading main class com.byteme.Main
   java.lang.UnsupportedClassVersionError: com/byteme/Main has been compiled by a more recent version of the Java Runtime (class file version 66.0), this version of the Java Runtime only recognizes class file versions up to 65.0"
   -> Run "mvn clean install" within /ByteMeStockTrader
