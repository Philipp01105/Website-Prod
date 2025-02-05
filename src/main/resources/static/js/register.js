document.addEventListener('DOMContentLoaded', function () {
    //console.log('DOM fully loaded and parsed.');
    const registerForm = document.getElementById('registerForm');
    if (!registerForm) {
        //console.error('Form with ID "registerForm" not found.');
        return;
    }

    registerForm.addEventListener('submit', function (event) {
        event.preventDefault();
        //console.log('Form submission event triggered.');

        const formData = new FormData(this);
        //console.log('Form data:', formData);

        fetch('/api/register', {
            method: 'POST',
            body: formData
        })
            .then(response => {
                //console.log('Response received:', response);
                return response.json();
            })
            .then(data => {
                console.log('Response data:', data);
                if (data.id && data.username) {
                    alert('Registration successful!');
                    window.location.href = '/login';
                } else {
                    alert('Registration failed: ' + (data.message || 'Unknown error'));
                }
            })
            .catch(error => {
                console.error('Error:', error);
            });
    });
});