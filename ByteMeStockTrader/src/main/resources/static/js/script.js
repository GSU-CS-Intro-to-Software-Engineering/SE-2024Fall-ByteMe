// script.js

document.addEventListener("DOMContentLoaded", function () {
  const brokerButtons = document.querySelectorAll(".broker-button");
  const nextButton = document.getElementById("nextButton");
  let selectedBroker = "";

  // Function to handle broker selection
  function selectBroker(button) {
    // Clear the selected style from all buttons
    brokerButtons.forEach((btn) => btn.classList.remove("selected"));

    // Apply selected style to the clicked button
    button.classList.add("selected");

    // Update selected broker and enable Next button
    selectedBroker = button.getAttribute("data-broker");
    sessionStorage.setItem("selectedBroker", selectedBroker);
    nextButton.disabled = false; // Enable Next button
  }

  // Add event listeners to broker buttons
  brokerButtons.forEach((button) => {
    button.addEventListener("click", () => selectBroker(button));
  });

  // Navigate to login page when "Next" is clicked
  nextButton.addEventListener("click", function () {
    if (selectedBroker) {
      window.location.href = "/login.html";
    }
  });

  // Load the selected broker on login page
  const brokerName = document.getElementById("brokerName");
  if (brokerName) {
    brokerName.textContent =
      sessionStorage.getItem("selectedBroker") || "Unknown Broker";
  }

  // Event listeners for "Enter" and "Back" buttons on the login page
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
