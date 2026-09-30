document.getElementById("searchBtn").addEventListener("click", searchFlights);

async function searchFlights(){

    const container = document.getElementById("resultBox");
    container.innerHTML = "<p>Searching flights... ✈️</p>";

    const input = {
        departure: document.getElementById("departure").value,
        arrival: document.getElementById("arrival").value,
        date: document.getElementById("date").value,
        preference: document.getElementById("preference").value
    };

    const response = await fetch("http://localhost:8080/search", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(input)
    });

    const data = await response.json();

    renderFlights(data);
}

function renderFlights(data) {
    const container = document.getElementById("resultBox");
    container.replaceChildren();

    data.forEach((option, index) => {
        const card = document.createElement("div");
        card.className = "flight-card";

        const title = document.createElement("h3");
        title.textContent = "✈️ Option " + (index + 1);

        const description = document.createElement("p");
        description.innerHTML = option.description.replace(/\n/g, "<br>");

        card.append(title, description);
        container.appendChild(card);
    });

}