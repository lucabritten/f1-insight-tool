document.getElementById("driverForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    const firstName = document.getElementById("firstName").value;
    const lastName = document.getElementById("lastName").value;
    const year = document.getElementById("year").value;

    const url = `http://localhost:8080/driver?
    firstName=${encodeURIComponent(firstName)}
    &lastName=${encodeURIComponent(lastName)}
    &year=${year}`;

    const resultBox = document.getElementById("result");
    resultBox.classList.add("hidden");

    try {
        const response = await fetch(url);

        if (!response.ok) {
            throw new Error("HTTP error " + response.status);
        }

        const data = await response.json();

        document.getElementById("res-name").textContent =
            data.firstName + " " + data.lastName;
        document.getElementById("res-team").textContent =
            data.teamName ?? "–";
        document.getElementById("res-year").textContent = year;

        resultBox.classList.remove("hidden");
    } catch (error) {
        alert("Error: " + error.message);
    }
});
