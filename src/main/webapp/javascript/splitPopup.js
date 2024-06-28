document.addEventListener('DOMContentLoaded', () => {
    const splitBtn = document.querySelector('.split-btn');
    const splitPopUp = document.querySelector('.split-pop-up');
    const splitOverlay = document.querySelector('.split-pop-up-overlay');
    const individualSplitPopUp = document.querySelector('.individual-split');
    const individualSplitOverlay = document.querySelector('.individual-split-overlay');
    const descriptionPopUp = document.querySelector('.description-pop-up');
    const descriptionOverlay = document.querySelector('.description-pop-up-overlay');
    const splitForm = document.getElementById('splitForm');
    const splitCountInput = document.getElementById('splitCount');
    const timeIndicationElement = document.getElementsByClassName('time-indication')[0];
    const individualSplitForm = document.getElementById('individualSplitForm');
    const descriptionForm = document.getElementById('descriptionForm');
    const role = sessionStorage.getItem('role');

    let currentSplitCount = 0;
    let totalSplits = 0;
    let totalTimeIndication = 0;
    let splitsData = [];
    let finalDescription = "";
    const homeworkId = new URLSearchParams(window.location.search).get('id'); // Get homework_id from URL

    // Show the first pop-up
    splitBtn.addEventListener('click', () => {
        splitPopUp.classList.add('visible');
        splitOverlay.classList.add('visible');
    });

    // Add event listeners for closing pop-ups when clicking outside of them
    splitOverlay.addEventListener('click', () => {
        splitPopUp.classList.remove('visible');
        splitOverlay.classList.remove('visible');
    });

    individualSplitOverlay.addEventListener('click', () => {
        individualSplitPopUp.classList.remove('visible');
        individualSplitOverlay.classList.remove('visible');
    });

    descriptionOverlay.addEventListener('click', () => {
        descriptionPopUp.classList.remove('visible');
        descriptionOverlay.classList.remove('visible');
    });

    // Handle form submission for the first pop-up
    splitForm.addEventListener('submit', (e) => {
        e.preventDefault(); // Prevent form submission

        totalSplits = parseInt(splitCountInput.value); // Get the number of splits
        totalTimeIndication = parseInt(timeIndicationElement.innerText.split(" ")[0]); // Get the total time indication

        if (totalSplits < 2) {
            alert('Number of splits must be greater than 1.');
            return;
        }

        if (totalTimeIndication <= 0) {
            alert('Total duration must be greater than 0.');
            return;
        }

        const splitDuration = Math.floor(totalTimeIndication / totalSplits); // Calculate individual split duration

        if (splitDuration <= 0) {
            alert('Split duration must be greater than 0.');
            return;
        }

        splitsData = Array.from({ length: totalSplits }, () => ({
            name: '',
            duration: splitDuration
        }));

        currentSplitCount = 1;
        splitPopUp.classList.remove('visible'); // Hide the first pop-up
        splitOverlay.classList.remove('visible');
        showIndividualSplitPopUp(); // Show the first individual split pop-up
    });

    // Function to show the individual split pop-up
    function showIndividualSplitPopUp() {
        individualSplitPopUp.classList.add('visible');
        individualSplitOverlay.classList.add('visible');

        // Initialize the form with the first split data
        setIndividualSplitFormData(splitsData[currentSplitCount - 1]);
    }

    // Function to set data in individual split form
    function setIndividualSplitFormData(splitData) {
        document.getElementById('splitName').value = splitData.name;
        document.getElementById('splitDuration').value = splitData.duration;
    }

    // Handle form submission for the individual split pop-up
    individualSplitForm.addEventListener('submit', (e) => {
        e.preventDefault(); // Prevent form submission

        const splitName = document.getElementById('splitName').value;
        const splitDuration = parseInt(document.getElementById('splitDuration').value);

        if (splitDuration <= 0) {
            alert('Split duration must be greater than 0.');
            return;
        }

        splitsData[currentSplitCount - 1] = {
            name: splitName,
            duration: splitDuration
        };

        currentSplitCount++;

        if (currentSplitCount > totalSplits) {
            const totalDuration = splitsData.reduce((acc, split) => acc + split.duration, 0);

            if (totalDuration !== totalTimeIndication) {
                alert(`Total duration of splits must be equal to ${totalTimeIndication}.`);
                currentSplitCount--;
                return;
            }

            individualSplitPopUp.classList.remove('visible'); // Hide the second pop-up
            individualSplitOverlay.classList.remove('visible');

            // Show the description pop-up
            descriptionPopUp.classList.add('visible');
            descriptionOverlay.classList.add('visible');
        } else {
            setIndividualSplitFormData(splitsData[currentSplitCount - 1]); // Update form for next split
        }
    });

    // Handle form submission for the description pop-up
    descriptionForm.addEventListener('submit', (e) => {
        e.preventDefault(); // Prevent form submission

        finalDescription = document.getElementById('descriptionInput').value;

        descriptionPopUp.classList.remove('visible');
        descriptionOverlay.classList.remove('visible');

        // Create the JSON object to send to the servlet
        const requestData = {
            split_count: totalSplits,
            splits: splitsData.map((split) => ({
                ...split
            })),
            description: finalDescription
        };

        // Log the requestData to verify its structure
        console.log('Request Data:', requestData);
        if (role === "teacher"){
            fetch(`/SomtodayHomeworkPlanner_war/api/splitHomeworkTeacher?homeworkId=${homeworkId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestData)
            })
                .then(response => response.json())
                .then(data => {
                    console.log('Success:', data);
                    alert('Split homework created successfully.');
                    // Redirect or update the UI as needed
                })
                .catch((error) => {
                    console.error('Error:', error);
                    alert('An error occurred while splitting the homework.');
                });
        } else if(role === "student"){
            fetch(`/SomtodayHomeworkPlanner_war/api/splitHomeworkStudent?homeworkId=${homeworkId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestData)
            })
                .then(response => response.json())
                .then(data => {
                    console.log('Success:', data);
                    alert('Split homework created successfully.');
                    // Redirect or update the UI as needed
                })
                .catch((error) => {
                    console.error('Error:', error);
                    alert('An error occurred while splitting the homework.');
                });
        }
    });
});
