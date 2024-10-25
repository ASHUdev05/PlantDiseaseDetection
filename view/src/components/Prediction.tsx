import React from "react";
import { useNavigate } from "react-router-dom";

interface PredictionProps {
  selectedFile: File | null;  // The uploaded file
  response: any;              // The server response
}

const Prediction: React.FC<PredictionProps> = ({ selectedFile, response }) => {
  const navigate = useNavigate();

  if (!selectedFile || !response) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <h2 className="text-xl font-bold text-red-500">
          No file uploaded or no response from server!
        </h2>
      </div>
    );
  }

  const imageUrl = URL.createObjectURL(selectedFile);

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-4">
      <div className="bg-white shadow-lg rounded-lg overflow-hidden max-w-xl w-full">
        <div className="p-6 text-center">
          <h1 className="text-2xl font-bold text-blue-600 mb-4">Prediction Results</h1>
          
          {/* Display the uploaded image */}
          <div className="mb-6">
            <img
              src={imageUrl}
              alt={selectedFile.name}
              className="w-full h-64 object-cover rounded-md shadow-md"
            />
            <p className="mt-2 text-sm text-gray-500">Uploaded Image: {selectedFile.name}</p>
          </div>
          
          {/* Display prediction results */}
          <div className="grid grid-cols-2 gap-4 bg-gray-100 p-4 rounded-md shadow-md mb-4">
            {/* Box for Class Name */}
            <div className="bg-blue-100 text-blue-800 font-semibold p-4 rounded-md shadow-inner">
              <h3 className="text-lg">{response.class}</h3>
            </div>

            {/* Box for Confidence */}
            <div className="bg-green-100 text-green-800 font-semibold p-4 rounded-md shadow-inner">
              <h3 className="text-lg">Confidence</h3>
              <p className="text-xl">{(response.confidence * 100).toFixed(2)}%</p>
            </div>
           </div>

          {/* Back to Home button */}
          <button
            onClick={() => navigate("/")}
            className="bg-blue-500 hover:bg-blue-600 text-white font-bold py-2 px-4 rounded-md transition-all duration-200"
          >
            Back to Home
          </button>
        </div>
      </div>
    </div>
  );
};

export default Prediction;