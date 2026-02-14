import { WeatherWithContext } from "./types.js";
import { callBackend, getElement } from "./utils.js";

export function initWeather(): void {
    const form = document.getElementById("weather-form") as HTMLFormElement | null;
    if (form) {
        form.addEventListener("submit", handleWeatherSubmit);
    }
}

async function handleWeatherSubmit(event: SubmitEvent): Promise<void> {
    event.preventDefault();

    const locationInput = document.getElementById("weather-location") as HTMLInputElement;
    const location = locationInput.value;

    const sessionInput = document.getElementById("weather-session") as HTMLInputElement;
    const session = sessionInput.value;

    const yearInput = document.getElementById("weather-year") as HTMLInputElement;
    const year = yearInput.value;

    const url = new URL("http://localhost:8080/weather");
    url.searchParams.append("location", location);
    url.searchParams.append("session", session);
    url.searchParams.append("year", year);

    const data: WeatherWithContext | null = await callBackend<WeatherWithContext>(url, "weather-loading-spinner", "weather-result");
    if(!data) return;

    renderWeather(data);
}

function renderWeather(data: WeatherWithContext): void {
    const meetingElem = getElement<HTMLElement>("weather-meeting-name");
    const countryElem = getElement<HTMLElement>("weather-country-name");
    const sessionElem = getElement<HTMLElement>("weather-session-name");

    const airTempElem = getElement<HTMLElement>("weather-air-temp");
    const trackTempElem = getElement<HTMLElement>("weather-track-temp");
    const humidityElem = getElement<HTMLElement>("weather-humidity");
    const windSpeedElem = getElement<HTMLElement>("weather-wind-speed");
    const windDirectionElem = getElement<HTMLElement>("weather-wind-direction");
    const rainfallElem = getElement<HTMLElement>("weather-rainfall");

    meetingElem.textContent = data.meetingName;
    countryElem.textContent = data.countryName;
    sessionElem.textContent = data.sessionName;

    airTempElem.textContent =
        data.weather.avgAirTemperature != null
            ? `${data.weather.avgAirTemperature.toFixed(2)} °C`
            : "–";

    trackTempElem.textContent =
        data.weather.avgTrackTemperature != null
            ? `${data.weather.avgTrackTemperature.toFixed(2)} °C`
            : "–";

    humidityElem.textContent =
        data.weather.avgHumidity != null
            ? `${data.weather.avgHumidity.toFixed(2)} %`
            : "–";

    windSpeedElem.textContent =
        data.weather.avgWindSpeed != null
            ? `${data.weather.avgWindSpeed.toFixed(2)} km/h`
            : "–";

    windDirectionElem.textContent =
        data.weather.avgWindDirection != null
            ? `${data.weather.avgWindDirection.toFixed(0)}°`
            : "–";

    rainfallElem.textContent =
        data.weather.isRainfall ? "Rain" : "Dry";
}