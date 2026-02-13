import { ApiError, Driver } from "./types.js";
import { showError, getElement } from "./utils.js";

export function initDriver(): void {
    const form = document.getElementById("driver-form");
    if (form) {
        form.addEventListener("submit", handleDriverSubmit);
    }
}

async function handleDriverSubmit(event: SubmitEvent): Promise<void> {
    event.preventDefault();

    const firstNameInput = document.getElementById("firstName") as HTMLInputElement;
    const firstName = firstNameInput.value;

    const lastNameValue = document.getElementById("lastName") as HTMLInputElement;
    const lastName = lastNameValue.value;
    
    const yearInput = document.getElementById("driver-year") as HTMLInputElement;
    const year = yearInput.value;

    const url = new URL("http://localhost:8080/driver");
    url.searchParams.append("first_name", firstName);
    url.searchParams.append("last_name", lastName);
    url.searchParams.append("year", year);

    const resultBox = document.getElementById("driver-result");
    if(!resultBox)
        throw new Error("driver-result element not found");
    resultBox.classList.add("hidden");

    const spinner = document.getElementById("loading-spinner");
    if (spinner) {
        spinner.classList.remove("hidden");
    }

    try {
        const response = await fetch(url);

        if (!response.ok) {
            const errorData: ApiError = await response.json();
            throw new Error(`(${errorData.error}) with message: ${errorData.message}`);
        }

        const data: Driver = await response.json();
        renderDriver(data, year);
        if (spinner) {
            spinner.classList.add("hidden");
        }
        resultBox.classList.remove("hidden");
    } catch (error) {
        if (spinner) {
            spinner.classList.add("hidden");
        }
        if (error instanceof Error)
            showError(error.message);
        else
            showError("An unknown error occurred")
    }
}

function renderDriver(data: Driver, year: string): void {
    console.log(data);
    const nameElem = getElement<HTMLElement>("res-name");
    const teamElem = getElement<HTMLElement>("res-team");
    const yearElem = getElement<HTMLElement>("res-year");

    nameElem.textContent = `${data.firstName} ${data.lastName}`;
    teamElem.textContent = data.teamName ?? "-";
    yearElem.textContent = year;
}