export interface Driver {
    firstName: string;
    lastName: string;
    teamName?: string;
}

export interface Lap {
    lapNumber: number;
    durationSector1: number;
    durationSector2: number;
    durationSector3: number;
    lapDuration: number;
    isPitOutLap: boolean;
}

export interface LapsWithContext {
    driverName: string;
    meetingName: string;
    sessionName: string;
    laps: Lap[];
}

export interface SessionResult {
    position: number;
    driverNumber: string;
    driverName: string;
    duration?: string[];
    gapToLeader?: string[];
    dsq?: boolean;
    dns?: boolean;
    dnf?: boolean;
}


export interface SessionResultWithContext {
    sessionName: string;
    results: SessionResult[];
}

export interface ApiError {
    error: string;
    message: string;
}

export interface WeatherWithContext {
    meetingName: string;
    countryName: string;
    sessionName: string;
    weather: Weather;
}

export interface Weather {
    sessionKey: number;
    meetingKey: number;
    avgAirTemperature: number;
    avgHumidity: number;
    isRainfall: boolean;
    avgTrackTemperature: number;
    avgWindDirection: number;
    avgWindSpeed: number;
}