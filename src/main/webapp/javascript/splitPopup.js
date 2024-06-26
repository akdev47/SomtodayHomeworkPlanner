
/* Pop-Ups ---------------------------------------------------------------------*/

document.addEventListener('DOMContentLoaded', () => {
    const splitBtn = document.querySelector('.split-btn');
    const splitPopUp = document.querySelector('.split-pop-up');
    const splitOverlay = document.querySelector('.split-pop-up-overlay');
    const scSubmitBtn = document.querySelector('.sc-submit-btn');
    const individualSplitPopUp = document.querySelector('.individual-split');
    const individualSplitOverlay = document.querySelector('.individual-split-overlay');
    const splitForm = document.getElementById('splitForm');
    const splitCountInput = document.getElementById('splitCount');
    const timeIndicationInput = document.getElementsByClassName('time-indication')[0];
    const individualSplitForm = document.getElementById('individualSplitForm');

    let currentSplitCount = 0;
    let totalSplits = 0;
    let totalTimeIndication = 0;
    let splitsData = [];

    // Show the first pop-up
    splitBtn.addEventListener('click', () => {
        splitPopUp.classList.add('visible');
        splitOverlay.classList.add('visible');
    });

    // Handle form submission for the first pop-up
    splitForm.addEventListener('submit', (e) => {
        e.preventDefault(); // Prevent form submission

        totalSplits = parseInt(splitCountInput.value); // Get the number of splits
        totalTimeIndication = parseInt(timeIndicationInput.value); // Get the total time indication

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
                return;
            }

            individualSplitPopUp.classList.remove('visible'); // Hide the second pop-up
            individualSplitOverlay.classList.remove('visible');

            // Process splitsData as needed (e.g., send to server, update UI)
            console.log('Splits data:', splitsData);
        } else {
            setIndividualSplitFormData(splitsData[currentSplitCount - 1]); // Update form for next split
        }
    });
});
