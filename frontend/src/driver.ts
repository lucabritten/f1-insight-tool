import { ApiError, Driver } from "./types.js";
import { showError, getElement, callBackend } from "./utils.js";

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

    const data: Driver | null = await callBackend<Driver>(url, "driver-loading-spinner", "driver-result");
    if(!data) return;

    renderDriver(data, year);

}

function renderDriver(data: Driver, year: string): void {
    const nameElem = getElement<HTMLElement>("res-name");
    const teamElem = getElement<HTMLElement>("res-team");
    const yearElem = getElement<HTMLElement>("res-year");

    nameElem.textContent = `${data.firstName} ${data.lastName}`;
    teamElem.textContent = data.teamName ?? "-";
    yearElem.textContent = year;
}