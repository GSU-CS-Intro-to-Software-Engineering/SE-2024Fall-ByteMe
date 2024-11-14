document.addEventListener("DOMContentLoaded", function () {
  const brokerButtons = document.querySelectorAll(".broker-button");
  const nextButton = document.getElementById("nextButton");
  let selectedBroker = "";

  // Function to handle broker selection
  function selectBroker(button) {
    brokerButtons.forEach((btn) => btn.classList.remove("selected"));
    button.classList.add("selected");

    selectedBroker = button.getAttribute("data-broker");
    sessionStorage.setItem("selectedBroker", selectedBroker);
    nextButton.disabled = false;
  }

  // Add event listeners to each broker button
  brokerButtons.forEach((button) => {
    button.addEventListener("click", () => selectBroker(button));
  });

  // Next button to navigate to login.html
  if (nextButton) {
    nextButton.addEventListener("click", function () {
      if (selectedBroker) {
        window.location.href = "/login.html";
      }
    });
  }

  // Display selected broker on login page
  const brokerName = document.getElementById("brokerName");
  if (brokerName) {
    brokerName.textContent = sessionStorage.getItem("selectedBroker") || "Unknown Broker";
  }

  // Enter button to navigate to stock selection page
  const enterButton = document.getElementById("enterButton");
  if (enterButton) {
    enterButton.addEventListener("click", function () {
      window.location.href = "/stock-selection.html";
    });
  }

  // Stock selection buttons on stock-selection.html
  const stockButtons = document.querySelectorAll(".stock-button");
  const stockNextButton = document.getElementById("stockNextButton");
  let selectedStock = "";

  // Function to handle stock selection
  function selectStock(button) {
    stockButtons.forEach((btn) => btn.classList.remove("selected"));
    button.classList.add("selected");

    selectedStock = button.getAttribute("data-stock");
    sessionStorage.setItem("selectedStock", selectedStock);
    stockNextButton.disabled = false; // Enable Next button

    // Map stock symbols to quote-box indexes
    const stockIndexMap = {
      "NVDA": 0,
      "AAPL": 1,
      "MSFT": 2,
      "TSLA": 3
    };

    stockButtons.forEach((button) => {
      button.addEventListener("click", () => selectStock(button));
    });

    // Set currentIndex based on selected stock and update focus
    currentIndex = stockIndexMap[selectedStock] ?? 0;
    updateFocus();
  }

  // Add event listeners to each stock button
  stockButtons.forEach((button) => {
    button.addEventListener("click", () => selectStock(button));
  });

  // Next button on stock-selection.html to navigate to portfolio.html
  if (stockNextButton) {
    stockNextButton.addEventListener("click", function () {
      if (selectedStock) {
        window.location.href = "/portfolio.html";
      }
    });
  }

  const backButton = document.getElementById("backButton");
    if (backButton) {
      backButton.addEventListener("click", function () {
        window.location.href = "/";
      });
    }

  // Display selected stock on portfolio.html
  const selectedStockElement = document.getElementById("selectedStock");
  const stockSymbolElement = document.getElementById("stockSymbol");
  if (selectedStockElement && stockSymbolElement) {
    const stock = sessionStorage.getItem("selectedStock");
    selectedStockElement.textContent = stock;
    stockSymbolElement.textContent = stock;
  }

  // Toggle Trading Status Button
  const tradingStatusButton = document.getElementById("tradingStatusButton");
  if (tradingStatusButton) {
    tradingStatusButton.addEventListener("click", function () {
      if (tradingStatusButton.classList.contains("active")) {
        tradingStatusButton.classList.remove("active");
        tradingStatusButton.classList.add("inactive");
        tradingStatusButton.textContent = "Deactivated";
        tradingStatusButton.style.backgroundColor = "#dc3545"; // Red
      } else {
        tradingStatusButton.classList.remove("inactive");
        tradingStatusButton.classList.add("active");
        tradingStatusButton.textContent = "Actively Trading";
        tradingStatusButton.style.backgroundColor = "#28a745"; // Green
      }
    });
  }

// Carousel Functionality
const carousel = document.querySelector(".carousel");
const quoteBoxes = document.querySelectorAll(".quote-box");
let currentIndex = 1;

// Set initial focus on the center item
quoteBoxes[currentIndex].classList.add("focused");

function updateFocus() {
  quoteBoxes.forEach((box, index) => {
    box.classList.toggle("focused", index === currentIndex);
  });
  // Ensure the scroll is smooth
  quoteBoxes[currentIndex].scrollIntoView({ behavior: "smooth", inline: "center" });
}

quoteBoxes.forEach((box, index) => {
  box.addEventListener("click", () => {
    currentIndex = index;
    updateFocus();
  });
});

let isDragging = false;
let startX = 0;

carousel.addEventListener("mousedown", (e) => {
  isDragging = true;
  startX = e.clientX;
});

carousel.addEventListener("mousemove", (e) => {
  if (!isDragging) return;
  const x = e.clientX;
  const walk = x - startX;

  if (walk > 50 && currentIndex > 0) {
    currentIndex--;
    updateFocus();
    isDragging = false;
  } else if (walk < -50 && currentIndex < quoteBoxes.length - 1) {
    currentIndex++;
    updateFocus();
    isDragging = false;
  }
});

carousel.addEventListener("mouseup", () => {
  isDragging = false;
});

carousel.addEventListener("touchstart", (e) => {
  isDragging = true;
  startX = e.touches[0].clientX;
});

carousel.addEventListener("touchmove", (e) => {
  if (!isDragging) return;
  const x = e.touches[0].clientX;
  const walk = x - startX;

  if (walk > 50 && currentIndex > 0) {
    currentIndex--;
    updateFocus();
    isDragging = false;
  } else if (walk < -50 && currentIndex < quoteBoxes.length - 1) {
    currentIndex++;
    updateFocus();
    isDragging = false;
  }
});

carousel.addEventListener("touchend", () => {
  isDragging = false;
});


});
