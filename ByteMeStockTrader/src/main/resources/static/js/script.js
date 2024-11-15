document.addEventListener("DOMContentLoaded", function () {
  // Broker Selection Logic
  const brokerButtons = document.querySelectorAll(".broker-button");
  const nextButton = document.getElementById("nextButton");
  let selectedBroker = "";

  function selectBroker(button) {
    brokerButtons.forEach((btn) => btn.classList.remove("selected"));
    button.classList.add("selected");

    selectedBroker = button.getAttribute("data-broker");
    sessionStorage.setItem("selectedBroker", selectedBroker);
    if (nextButton) nextButton.disabled = false;
  }

  brokerButtons.forEach((button) =>
    button.addEventListener("click", () => selectBroker(button))
  );

  if (nextButton) {
    nextButton.addEventListener("click", function () {
      if (selectedBroker) {
        window.location.href = "/login.html";
      }
    });
  }

  const brokerName = document.getElementById("brokerName");
  if (brokerName) {
    brokerName.textContent = sessionStorage.getItem("selectedBroker") || "Unknown Broker";
  }

  // Login Form Logic
  const loginForm = document.getElementById("loginForm");
  let tradingType = "paper"; 

  const paperButton = document.getElementById("paperButton");
  const cashButton = document.getElementById("cashButton");

  function updateTradingTypeButton() {
    if (tradingType === "paper") {
      paperButton.classList.add("selected");
      cashButton.classList.remove("selected");
    } else {
      cashButton.classList.add("selected");
      paperButton.classList.remove("selected");
    }
  }

  if(paperButton && cashButton) {
    updateTradingTypeButton();
    // Set up event listeners for trading type toggle
    paperButton.addEventListener("click", () => {
      tradingType = "paper";
      updateTradingTypeButton();
    });

    cashButton.addEventListener("click", () => {
      tradingType = "cash";
      updateTradingTypeButton();
    });
  }

  if (loginForm) {
    loginForm.addEventListener("submit", async (event) => {
      event.preventDefault();

      const apiKey = document.getElementById("apiKey").value;
      const apiSecret = document.getElementById("apiSecret").value;

      try {
        const response = await fetch("/api/authenticate", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ apiKey, apiSecret, tradingType}),
        });

        if (response.ok) {
          window.location.href = "/stock-selection.html";
        } else {
          alert("Invalid API Key or Secret Key. Please try again.");
        }
      } catch (error) {
        console.error("Error authenticating with Alpaca:", error);
        alert("An error occurred. Please try again.");
      }
    });
  }

  const infoIcon = document.getElementById("infoIcon");
  const infoModal = document.getElementById("infoModal");
  const closeModalButton = document.getElementById("closeModalButton");

  // Show the modal when the info icon is clicked
  if (infoIcon) {
  infoIcon.addEventListener("click", () => {
    infoModal.style.display = "flex"; // Display the modal
  });
}

  // Close the modal when the close button is clicked
  if (closeModalButton) {
  closeModalButton.addEventListener("click", () => {
    infoModal.style.display = "none"; // Hide the modal
  });
}

  // Close the modal if the user clicks outside the modal content
  window.addEventListener("click", (event) => {
    if (event.target === infoModal) {
      infoModal.style.display = "none"; // Hide the modal
    }
  });

  // Stock Selection Logic
  const stockButtons = document.querySelectorAll(".stock-button");
  const stockNextButton = document.getElementById("stockNextButton");
  let selectedStock = "";

  // Map stocks to their corresponding carousel indexes
  const stockIndexMap = {
    "NVDA": 0,
    "AAPL": 1,
    "MSFT": 2,
    "TSLA": 3
  };

  function selectStock(button) {
    stockButtons.forEach((btn) => btn.classList.remove("selected"));
    button.classList.add("selected");

    selectedStock = button.getAttribute("data-stock");
    sessionStorage.setItem("selectedStock", selectedStock);

    if (stockNextButton) stockNextButton.disabled = false;

    // Automatically focus the corresponding carousel item
    const stockIndex = stockIndexMap[selectedStock];
    if (stockIndex !== undefined) {
      currentIndex = stockIndex;
      updateFocus();
    }
  }

  stockButtons.forEach((button) =>
    button.addEventListener("click", () => selectStock(button))
  );

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
        tradingStatusButton.style.backgroundColor = "#dc3545";
      } else {
        tradingStatusButton.classList.remove("inactive");
        tradingStatusButton.classList.add("active");
        tradingStatusButton.textContent = "Actively Trading";
        tradingStatusButton.style.backgroundColor = "#28a745";
      }
    });
  }

  // Carousel Logic
  const carousel = document.querySelector(".carousel");
  const quoteBoxes = document.querySelectorAll(".quote-box");
  let currentIndex = 1;

  if (carousel && quoteBoxes.length > 0) {
    quoteBoxes[currentIndex].classList.add("focused");

    function updateFocus() {
      quoteBoxes.forEach((box, index) =>
        box.classList.toggle("focused", index === currentIndex)
      );
      quoteBoxes[currentIndex].scrollIntoView({ behavior: "smooth", inline: "center" });
    }

    quoteBoxes.forEach((box, index) => {
      box.addEventListener("click", () => {
        currentIndex = index; // Update currentIndex based on the clicked box
        updateFocus(); // Reapply focus
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
      const walk = e.clientX - startX;
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

    carousel.addEventListener("mouseup", () => (isDragging = false));

    carousel.addEventListener("touchstart", (e) => {
      isDragging = true;
      startX = e.touches[0].clientX;
    });

    carousel.addEventListener("touchmove", (e) => {
      if (!isDragging) return;
      const walk = e.touches[0].clientX - startX;
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

    carousel.addEventListener("touchend", () => (isDragging = false));
  }
});
