package htwsaar.nordpol.service.driver;

import htwsaar.nordpol.domain.Driver;

public interface IDriverService {
    Driver getDriverByNameAndYear(String firstName, String lastName, int year);
    Driver getDriverByNumberAndYear(int number, int year);
}
