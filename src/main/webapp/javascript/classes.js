document.addEventListener('DOMContentLoaded', function() {
    // Fetch classes and update frontend
    const role = sessionStorage.getItem('role');
    const personId = sessionStorage.getItem('personId');
    const personName = sessionStorage.getItem('personName');
    const welcomeMessage = document.getElementById('welcome-message');
    const addClassButton = document.getElementById('addClassButton');
    const editClassButton = document.getElementById('editClassButton');
    const cardContainer = document.getElementById('card-container');
    const listContainer = document.getElementById('list-container');
    const sidebar = document.getElementById('sidebar');

    if (personId) {
        profileLink.href = `profile.html?personId=${personId}`;
    }

    // Update welcome message
    if (role === 'admin') {
        welcomeMessage.textContent = `Welcome, ${personName}!`;
        sidebar.classList.add('green');
    } else if (role === 'teacher') {
        welcomeMessage.textContent = `Welcome, ${personName}!`;
        editClassButton.style.display = 'none';
        addClassButton.style.display = 'none';
        sidebar.classList.add('yellow');
    } else {
        welcomeMessage.textContent = `Welcome, ${personName}!`;
        addClassButton.style.display = 'none';
        editClassButton.style.display = 'none';
        sidebar.classList.add('red');
    }

    function getRandomGradient() {
        const colors = [
            '#ff9a9e', '#fad0c4', '#fad0c4',
            '#a18cd1', '#fbc2eb', '#a6c1ee',
            '#84fab0', '#8fd3f4', '#fccb90',
            '#a1c4fd', '#c2e9fb', '#d4fc79',
            '#96e6a1', '#fbc2eb', '#e0c3fc'
        ];
        const randomIndex = () => Math.floor(Math.random() * colors.length);
        return `linear-gradient(135deg, ${colors[randomIndex()]} 0%, ${colors[randomIndex()]} 100%)`;
    }

    function createCard(classData) {
        const card = document.createElement('div');
        card.className = 'card';

        const imgSection = document.createElement('div');
        imgSection.className = 'img-section';
        if (classData.has_profile_picture) {
            const img = document.createElement('img');
            img.src = `api/getClassPicture?classId=${classData.class_id}`;
            img.alt = `${classData.class_name} Profile Picture`;
            imgSection.appendChild(img);

            img.onerror = () => {
                imgSection.style.background = getRandomGradient();
                img.remove();
            };
        } else {
            imgSection.style.background = getRandomGradient();
        }

        const textSection = document.createElement('div');
        textSection.className = 'text-section';

        const h2 = document.createElement('h2');
        const link = document.createElement('a');
        link.href = `class.html?classId=${classData.class_id}`;
        link.textContent = classData.class_name;
        h2.appendChild(link);
        textSection.appendChild(h2);

        card.appendChild(imgSection);
        card.appendChild(textSection);

        return card;
    }

    function createListItem(classData) {
        const listItem = document.createElement('div');
        listItem.className = 'list-item';

        const colorDot = document.createElement('div');
        colorDot.className = 'color-dot';
        colorDot.style.background = getRandomGradient();
        listItem.appendChild(colorDot);

        const listText = document.createElement('div');
        listText.className = 'list-text';
        const link = document.createElement('a');
        link.href = `class.html?classId=${classData.class_id}`;
        link.textContent = classData.class_name;
        listText.appendChild(link);
        listItem.appendChild(listText);

        return listItem;
    }

    function fetchClasses() {
        console.log('Fetching classes...');
        fetch(`api/fetchClasses?role=${role}&personId=${personId}`)
            .then(response => {
                console.log('Fetch response:', response);
                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }

                return response.json();
            })
            .then(classes => {
                console.log('Classes fetched:', classes);
                cardContainer.innerHTML = '';
                listContainer.innerHTML = '';
                classes.forEach(classData => {
                    const card = createCard(classData);
                    cardContainer.appendChild(card);

                    const listItem = createListItem(classData);
                    listContainer.appendChild(listItem);
                });
            })
            .catch(error => console.error('Error fetching classes:', error));
    }

    if (role && personId) {
        setTimeout(fetchClasses, 50);
    } else {
        console.error('Missing role or personId in session storage');
    }

    // Switching between card and list views
    const toggleCheckbox = document.getElementById('toggle');
    toggleCheckbox.addEventListener('change', function() {
        if (toggleCheckbox.checked) {
            cardContainer.style.display = 'none';
            listContainer.style.display = 'flex';
        } else {
            cardContainer.style.display = 'grid';
            listContainer.style.display = 'none';
        }
    });
});
