// script

document.addEventListener("DOMContentLoaded", function () {
  const brokerButtons = document.querySelectorAll(".broker-button");
  const nextButton = document.getElementById("nextButton");
  let selectedBroker = "";

  //  selection
  function selectBroker(button) {
    
    brokerButtons.forEach((btn) => btn.classList.remove("selected"));

    
    button.classList.add("selected");

    
    selectedBroker = button.getAttribute("data-broker");
    sessionStorage.setItem("selectedBroker", selectedBroker);
    nextButton.disabled = false; // Enable Next button
  }

  
  brokerButtons.forEach((button) => {
    button.addEventListener("click", () => selectBroker(button));
  });

  
  nextButton.addEventListener("click", function () {
    if (selectedBroker) {
      window.location.href = "/login.html";
    }
  });

  
  const brokerName = document.getElementById("brokerName");
  if (brokerName) {
    brokerName.textContent =
      sessionStorage.getItem("selectedBroker") || "Unknown Broker";
  }

  
  const enterButton = document.getElementById("enterButton");
  const backButton = document.getElementById("backButton");

  if (enterButton) {
    enterButton.addEventListener("click", function () {
      alert("Logging in...");
      // Add login logic here if needed
    });
  }

  if (backButton) {
    backButton.addEventListener("click", function () {
      window.location.href = "/index.html";
    });
  }
});
