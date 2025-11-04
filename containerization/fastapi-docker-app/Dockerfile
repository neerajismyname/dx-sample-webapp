# 1. Base Image: Use a slim Python image for smaller size
FROM python:3.11-slim

# 2. Set working directory in the container
WORKDIR /app

# 3. Copy only the requirements file first for effective caching
COPY requirements.txt /app

# 4. Install dependencies
# --no-cache-dir ensures we don't store cache unnecessarily, reducing image size
RUN pip install --no-cache-dir -r requirements.txt

# 5. Copy the rest of the application code
COPY . /app

# 6. Expose the port the application will run on
EXPOSE 80

# 7. Command to run the application using Uvicorn
# 'main:app' means the variable 'app' inside the 'main.py' file.
# --host 0.0.0.0 is crucial to make the app accessible from outside the container.
# --port 80 matches the EXPOSE instruction.
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "80"]