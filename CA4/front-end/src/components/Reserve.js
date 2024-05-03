import { useState, useEffect } from 'react';

import CompleteReserveModal from './CompleteReserveModal';

const getCurrentDate = () => {
  const date = new Date();
  const year = date.getFullYear();
  let month = date.getMonth() + 1;
  let day = date.getDate();

  month = month < 10 ? '0' + month : month;
  day = day < 10 ? '0' + day : day;

  return `${year}-${month}-${day}`;
};

function Reserve({ maxSeatsNumber, address }) {
  const [selectedDate, setSelectedDate] = useState('');
  const [selectedTime, setSelectedTime] = useState('');
  const [selectedSeat, setSelectedSeat] = useState(0);
  const [filteredAvailableTimes, setFilteredAvailableTimes] = useState([]);
  const [isButtonDisabled, setIsButtonDisabled] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [availableTimes, setAvailableTimes] = useState([]);

  useEffect(() => {
    fetch('/api/restaurant/available-times', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error('Failed to fetch available times');
        }
        return response.json();
      })
      .then((data) => {
        setAvailableTimes(data);
        console.log('Success:', data);
      })
      .catch(error => {
        console.error('Error fetching available times:', error);
      });
  }, []);

  useEffect(() => {
    if (selectedDate) {
      const filteredTimes = availableTimes.filter(time => time.date === selectedDate);
      setFilteredAvailableTimes(filteredTimes);
      setErrorMessage('');
    } else {
      setFilteredAvailableTimes([]);
    }
  }, [selectedDate, availableTimes]);

  useEffect(() => {
    setIsButtonDisabled(!(selectedDate && selectedTime && selectedSeat));
  }, [selectedDate, selectedTime, selectedSeat]);

  const seatOptions = [<option key="empty" value=""></option>];
  for (let i = 1; i <= maxSeatsNumber; i++) {
    seatOptions.push(<option key={i} value={i}>{i}</option>);
  }

  const handleDateChange = (e) => {
    const selected = new Date(e.target.value);
    const today = new Date();
    const oneMonthFromNow = new Date(today.getFullYear(), today.getMonth() + 1, today.getDate());

    if (selected > oneMonthFromNow) {
      const maxDate = `${oneMonthFromNow.getFullYear()}-${('0' + (oneMonthFromNow.getMonth() + 1)).slice(-2)}-${('0' + oneMonthFromNow.getDate()).slice(-2)}`;
      setErrorMessage(`The maximum possible date for reservation is ${maxDate}`);
      setSelectedDate('');
    } else {
      setSelectedDate(e.target.value);
    }
  };

  const handleTimeChange = (e) => {
    setSelectedTime(e.target.value);
  };

  const handleSeatChange = (e) => {
    if (e !== "") {
      setSelectedSeat(e.target.value);
    }
  };

  return (
    <section className="col-lg">
      <h2 className="fw-semibold fs-5 mb-4">Reserve Table</h2>
      <form id="reserve-form">
        <div className="ps-2 ps-sm-0">
          <label htmlFor="people">For</label>
          <select className="form-select mx-1" name="people" id="people" onChange={handleSeatChange} value={selectedSeat}>{seatOptions}</select>
          <span>people, </span>
          <br className="d-sm-none" />
          <label className="pt-3 pt-sm-0" htmlFor="date"> on date </label>
          <input className="form-control mx-1" type="date" name="date" id="date" min={getCurrentDate()} required onChange={handleDateChange} />
        </div>

        {errorMessage && (
          <p className="miz-text-red my-4">{errorMessage}</p>
        )}

        {!errorMessage && selectedDate && (
          <>
            {filteredAvailableTimes.length > 0 ? (
              <>
                <p className="mt-3 mb-2">Available Times</p>ّ
                <div className="row row-cols-4 row-cols-md-5 row-cols-lg-4 g-2">
                  {filteredAvailableTimes.map((time, index) => (
                    <div key={index} className="col">
                      <input className="btn-check" type="radio" name="time" value={time.time} id={`time${index + 1}`} required onChange={handleTimeChange} />
                      <label className="available-time d-block btn" htmlFor={`time${index + 1}`}>{time.time}</label>
                    </div>
                  ))}
                </div>
                <p id="one-hour-text" className="miz-text-red my-2">
                  You will reserve this table only for
                  <span className="text-decoration-underline"> one </span>
                  hour, for more time please contact the restaurant.
                </p>
              </>
            ) : (
              <p className="my-4">No Table is available on this date.</p>
            )}
          </>
        )}

        {!errorMessage && !selectedDate && (
          <div>
            <p className="mt-3 mb-2">Available Times</p>
            <p className="miz-text-red my-2">Select the number of people and date.</p>
          </div>
        )}
        <button type="submit" className="miz-button disabled-button w-100" data-bs-toggle="modal" data-bs-target="#completeReservation" disabled={isButtonDisabled}>Complete the Reservation</button>
      </form>

      <CompleteReserveModal country={address.country} city={address.city} street={address.street} tableNumber={selectedSeat} time={selectedTime} />
    </section>
  );
}

export default Reserve;
