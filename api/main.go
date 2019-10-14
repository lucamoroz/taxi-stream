package main

import (
	"encoding/json"
	"github.com/gorilla/mux"
	"io"
	"io/ioutil"
	"log"
	"net/http"
	"os"
)

func main() {
	// resolve port to use
	port := os.Getenv("HTTP_PORT")
	if port == "" {
		log.Println("No HTTP_PORT environment variable specified. Using default port 3000")
		port = "3000" // default port
	}
	demoDataAccess() // demo accessing the json data from the external file

	// setup routing
	r := mux.NewRouter()
	r.HandleFunc("/health", HealthHandler)
	r.HandleFunc("/color/random", RandomColorHandler)
	http.Handle("/", r)

	// start api server (blocking)
	if err := http.ListenAndServe(":"+port, nil); err != nil {
		panic(err)
	}
}

// RandomColorHandler will "proxy" the request and call the color-service for a random color
func RandomColorHandler(writer http.ResponseWriter, request *http.Request) {
	resp, err := http.Get("http://color-service:4000/color")
	if err != nil {
		writer.WriteHeader(http.StatusInternalServerError)
		log.Println("Failed to fetch random color")
		return
	}
	writer.Header().Add("Content-Type", resp.Header.Get("Content-Type"))
	_, _ = io.Copy(writer, resp.Body)
}

// ColorData is simply a typing for the sample json data in data.json
type ColorData struct {
	Colors []struct {
		Color string `json:"color"`
		Value string `json:"value"`
	} `json:"colors"`
}

// demoDataAccess shows that you can mount a data file into the container and access it
func demoDataAccess() {
	bytes, err := ioutil.ReadFile("/app/data.json")
	if err != nil {
		panic(err)
	}
	var colorData ColorData
	if err = json.Unmarshal(bytes, &colorData); err != nil {
		panic(err)
	}
	log.Println("The first color: ", colorData.Colors[0])
}

// HealthHandler responds to all requests with "ok" to signal that this application is ready to receive traffic
func HealthHandler(writer http.ResponseWriter, request *http.Request) {
	writer.Header().Add("Access-Control-Allow-Origin", "*") // CORS example ... allow requests from all domains
	_, err := writer.Write([]byte("ok"))
	if err != nil {
		writer.WriteHeader(http.StatusInternalServerError)
		log.Println("Failed to produce health check")
	}
}
