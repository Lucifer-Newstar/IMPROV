const workoutPlans = {
  Chest: ['Push-ups', 'Bench press', 'Incline dumbbell fly'],
  Arms: ['Bicep curls', 'Tricep dips', 'Hammer curls'],
  Back: ['Pull-ups', 'Bent-over row', 'Lat pulldown'],
  Legs: ['Squats', 'Walking lunges', 'Leg press'],
  'Full Body': ['Jumping jacks', 'Plank', 'Mountain climbers']
};

let selectedCategory = 'Full Body';

function navigateToExercises(muscle) {
  // Navigate to exercises detail page
  window.location.href = 'exercises-detail.html?muscle=' + muscle;
}

function renderWorkoutPlan(category = 'Full Body') {
  selectedCategory = category;
  const planTitle = document.getElementById('plan-title');
  const workoutList = document.getElementById('workout-list');
  const usernameText = document.getElementById('username-text');

  if (!planTitle || !workoutList || !usernameText) {
    return;
  }

  const exercises = workoutPlans[category] || workoutPlans['Full Body'];
  planTitle.textContent = `${category} Workout Plan`;
  workoutList.innerHTML = exercises.map((exercise) => `<li>${exercise}</li>`).join('');
  usernameText.textContent = `Ready for ${category}`;
}

function selectCategory(category) {
  renderWorkoutPlan(category);
}

function startWorkout() {
  const message = `Starting your ${selectedCategory} workout!`;
  window.alert(message);
}

window.selectCategory = selectCategory;
window.startWorkout = startWorkout;
window.navigateToExercises = navigateToExercises;

window.addEventListener('DOMContentLoaded', () => {
  renderWorkoutPlan(selectedCategory);
});
