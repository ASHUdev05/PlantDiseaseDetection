import axios from "axios";
import { Component, ChangeEvent } from "react";
import { BrowserRouter as Router, Route, Routes, useNavigate } from "react-router-dom";
import Prediction from "./components/Prediction";

interface AppState {
  selectedFile: File | null;
  response: any; // To store the backend response
}

class App extends Component<{}, AppState> {
  state: AppState = {
    selectedFile: null,
    response: null, // Initially, no response
  };

  // On file select (from the pop up)
  onFileChange = (event: ChangeEvent<HTMLInputElement>): void => {
    if (event.target.files && event.target.files.length > 0) {
      this.setState({
        selectedFile: event.target.files[0],
      });
    }
  };

  // On file upload (click the upload button)
  onFileUpload = async (navigate: any): Promise<void> => {
    const { selectedFile } = this.state;
    if (!selectedFile) {
      return;
    }

    // Create a new FormData object
    const formData = new FormData();
    formData.append("file", selectedFile, selectedFile.name);

    try {
      // Send the POST request to the backend with the formData
      const response = await axios.post(
        "http://localhost:6789/predict",
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        }
      );

      // Update the state with the backend response
      this.setState({
        response: response.data,
      });

      // Navigate to the prediction page
      navigate("/prediction");
    } catch (error) {
      console.error("There was an error uploading the file!", error);
    }
  };

  render(): JSX.Element {
    return (
      <Router>
        <Routes>
          <Route
            path="/"
            element={
              <Home onFileChange={this.onFileChange} onFileUpload={this.onFileUpload} />
            }
          />
          <Route
            path="/prediction"
            element={
              <Prediction
                selectedFile={this.state.selectedFile}
                response={this.state.response}
              />
            }
          />
        </Routes>
      </Router>
    );
  }
}

const Home: React.FC<{ onFileChange: any; onFileUpload: any }> = ({ onFileChange, onFileUpload }) => {
  const navigate = useNavigate();
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center items-center">
      <div className="w-full max-w-md mx-auto bg-white shadow-md rounded-md p-8">
        <h1 className="text-3xl font-bold text-center text-blue-500 mb-6">
          File Upload and Prediction
        </h1>
        <div className="mb-4">
          <input
            type="file"
            onChange={onFileChange}
            className="block w-full text-sm text-gray-900 border border-gray-300 rounded-md cursor-pointer bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-400"
          />
        </div>
        <button
          onClick={() => onFileUpload(navigate)}
          className="w-full bg-blue-500 hover:bg-blue-600 text-white font-bold py-2 px-4 rounded-md transition-all duration-200"
        >
          Upload!
        </button>
      </div>
    </div>
  );
};

export default App;