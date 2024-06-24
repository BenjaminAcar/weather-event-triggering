# Weather Event System: Event-Driven Architecture

This repository contains an event-driven architecture for notifying cars about weather condition changes.

## Folder Structure

- **data-container**: Contains all the agents responsible for acquiring, storing, and transferring the data.
- **prediction-container**: Contains the agent responsible for predicting the current weather condition.
- **car-container**: Contains an agent that can be deployed on a vehicle, listening for prediction changes to inform the vehicle.

## Setup Instructions

### Step 1: Download Data
To make everything work, we need to download the data used for the experiment. You can find it [here](https://www.kaggle.com/datasets/ananthr1/weather-prediction).

The interesting data is the CSV called `forecast_data.csv`. When downloaded, copy it twice:
- To the folder `data-container/data`
- To the folder `prediction-container/data`

### Step 2: Create the Base Model
Navigate to the `create_model.py` in the `prediction-container`, activate your virtual environment, and ensure you have installed the following libraries:
- `joblib`
- `numpy`
- `pandas`
- `scikit-learn`
- `imbalanced-learn`

Run the script via:
```bash
python create_model.py
```
Now the model is created, and we can continue.

### Step 3: Set Up OPACA Framework
Download the OPACA framework from the official repository and copy all of the folders to the `opaca-core/examples` folder to provide all the dependencies directly.

### Step 4: Build and Deploy Containers
Navigate to the `data-container` folder and run:
```bash
mvn install
docker build -t data-container .
```
Do the same for the `car-container`:
```bash
mvn install
docker build -t car-container .
```
Finally, navigate to the `prediction-container` and run:
```bash
docker build -t prediction-container .
```

### Step 5: Run OPACA Runtime Platform
Open the Swagger-UI and start 3 new containers using the image names chosen:
- `car-container`
- `prediction-container`
- `data-container`

If needed, read the OPACA documentation to understand how to achieve this. Check that everything worked and that the containers are running.

### Step 6: Trigger Data Gathering
Trigger the action `GatherData` for the agent `station-`.

### Logs and Observations
You can look into the logs of the containers to see the following:
- The `car-container` listens to prediction changes and retrieves new predictions whenever available.
- The `data-container` gathers new data and transfers it to the `prediction-container` when new data arrives. This is simulated by waiting a certain time before processing the next row of the dataset.
- The `prediction-container` receives new data periodically and predicts the current condition.
