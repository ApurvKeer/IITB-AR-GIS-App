import pandas as pd
import numpy as np
import os



def get_place_in_view(lat, lon, orientation, name):
    """
    Get the first place within the specified field of view and distance.
    
    Parameters:
        lat (float): Latitude of the mobile device.
        lon (float): Longitude of the mobile device.
        orientation (float): Orientation of the device w.r.t. geographic north (degrees).
        name (string): Name of the place
        df (pd.DataFrame): DataFrame containing columns ['name', 'latitude', 'longitude'].
    
    Returns:
        str: Name, distance and angle to turn of the place, or None if no place matches.
    """
    # read the data file
    script_dir = os.path.dirname(__file__)
    # Build the full path to the CSV file
    csv_path = os.path.join(script_dir, "data.csv")
    df = pd.read_csv(csv_path)

    # Convert formats to float
    lat = float(lat)
    lon = float(lon)
    orientation = float(orientation)

    # Earth's radius in meters
    R = 6371000  
    
    # Convert angles to radians
    lat, lon, orientation = np.radians([lat, lon, orientation])
    
    def haversine(lat1, lon1, lat2, lon2):
        """Calculate the great-circle distance between two points on a sphere."""
        dlat = lat2 - lat1
        dlon = lon2 - lon1
        a = np.sin(dlat / 2)**2 + np.cos(lat1) * np.cos(lat2) * np.sin(dlon / 2)**2
        c = 2 * np.arctan2(np.sqrt(a), np.sqrt(1 - a))
        return R * c
    
    def calculate_bearing(lat1, lon1, lat2, lon2):
        """Calculate the bearing from one point to another."""
        y = np.sin(lon2 - lon1) * np.cos(lat2)
        x = np.cos(lat1) * np.sin(lat2) - np.sin(lat1) * np.cos(lat2) * np.cos(lon2 - lon1)
        return np.arctan2(y, x)
    
    matches = []
    
    for _, row in df.iterrows():
        if (row['name'] == name):

            place_lat, place_lon = np.radians([row['latitude'], row['longitude']])

            # Calculate distance
            distance = haversine(lat, lon, place_lat, place_lon)

            # Calculate bearing
            bearing = calculate_bearing(lat, lon, place_lat, place_lon)

            # Normalize bearing and orientation to [0, 2*pi]
            bearing = (bearing + 2 * np.pi) % (2 * np.pi)
            orientation = (orientation + 2 * np.pi) % (2 * np.pi)

            # Find turn angle
            turn_angle = ((bearing - orientation + np.pi) % (2 * np.pi) - np.pi)*180/np.pi

            # Select that row
            matches.append((row['name'], distance, turn_angle, row['other_tags']))
    
    # return matches
    if matches:
        return matches[0]
    
    return "No Match"

def get_suggestions():
    # read the data file
    script_dir = os.path.dirname(__file__)
    # Build the full path to the CSV file
    csv_path = os.path.join(script_dir, "data.csv")
    df = pd.read_csv(csv_path)

    return df['name'].tolist()