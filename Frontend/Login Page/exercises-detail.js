// Comprehensive exercise database organized by muscle groups
const exerciseDatabase = {
  chest: [
    {
      name: 'Push-ups',
      target: 'chest',
      equipment: 'body weight',
      instructions: 'Start in a plank position. Lower your body until your chest nearly touches the floor. Push yourself back up to the starting position.',
      difficulty: 'beginner'
    },
    {
      name: 'Bench Press',
      target: 'chest',
      equipment: 'barbell',
      instructions: 'Lie on a flat bench with feet on the floor. Press the barbell upward from chest level, extending arms fully. Lower back to chest level.',
      difficulty: 'intermediate'
    },
    {
      name: 'Incline Dumbbell Press',
      target: 'chest',
      equipment: 'dumbbell',
      instructions: 'Set bench to incline position. Press dumbbells upward and slightly forward. Lower with control back to shoulder height.',
      difficulty: 'intermediate'
    },
    {
      name: 'Cable Flye',
      target: 'chest',
      equipment: 'cable machine',
      instructions: 'Set cables to chest height. Stand in the center with slight knee bend. Bring hands together in a controlled arc motion.',
      difficulty: 'beginner'
    },
    {
      name: 'Dumbbell Flye',
      target: 'chest',
      equipment: 'dumbbell',
      instructions: 'Lie on bench with dumbbells above chest. Lower arms in an arc motion until you feel a chest stretch. Return to start position.',
      difficulty: 'intermediate'
    }
  ],
  back: [
    {
      name: 'Pull-ups',
      target: 'back',
      equipment: 'pull-up bar',
      instructions: 'Grip the bar with hands shoulder-width apart. Pull your body upward until chest approaches the bar. Lower with control.',
      difficulty: 'advanced'
    },
    {
      name: 'Bent Over Barbell Row',
      target: 'back',
      equipment: 'barbell',
      instructions: 'Bend at hips with slight knee bend. Pull barbell to lower chest. Maintain a straight back throughout the movement.',
      difficulty: 'intermediate'
    },
    {
      name: 'Lat Pulldown',
      target: 'back',
      equipment: 'cable machine',
      instructions: 'Sit at machine with feet on floor. Pull the bar down to upper chest level. Squeeze shoulder blades together.',
      difficulty: 'beginner'
    },
    {
      name: 'Single Arm Dumbbell Row',
      target: 'back',
      equipment: 'dumbbell',
      instructions: 'Place knee on bench. Row dumbbell to hip level. Focus on squeezing back muscles at the top of the movement.',
      difficulty: 'intermediate'
    },
    {
      name: 'Face Pulls',
      target: 'back',
      equipment: 'cable machine',
      instructions: 'Attach rope to high pulley. Pull rope toward face, separating ends. Excellent for rear deltoids and upper back.',
      difficulty: 'beginner'
    }
  ],
  shoulders: [
    {
      name: 'Overhead Press',
      target: 'shoulders',
      equipment: 'barbell',
      instructions: 'Stand with barbell at shoulder height. Press upward explosively while maintaining core tension. Lower back to start.',
      difficulty: 'intermediate'
    },
    {
      name: 'Lateral Raise',
      target: 'shoulders',
      equipment: 'dumbbell',
      instructions: 'Stand with dumbbells at sides. Raise arms to shoulder height in a controlled motion. Lower back down slowly.',
      difficulty: 'beginner'
    },
    {
      name: 'Dumbbell Shoulder Press',
      target: 'shoulders',
      equipment: 'dumbbell',
      instructions: 'Sit on bench with dumbbells at shoulder height. Press upward and slightly forward. Lower back to shoulders.',
      difficulty: 'beginner'
    },
    {
      name: 'Machine Shoulder Press',
      target: 'shoulders',
      equipment: 'machine',
      instructions: 'Sit with back against pad. Adjust height so handles are at shoulder level. Press forward and upward.',
      difficulty: 'beginner'
    },
    {
      name: 'Upright Row',
      target: 'shoulders',
      equipment: 'barbell',
      instructions: 'Hold barbell at hip level. Pull upward keeping elbows high and wide. Stop at upper chest level.',
      difficulty: 'intermediate'
    }
  ],
  biceps: [
    {
      name: 'Barbell Curl',
      target: 'biceps',
      equipment: 'barbell',
      instructions: 'Stand with feet shoulder-width apart. Curl barbell up to chest height. Lower with control to starting position.',
      difficulty: 'beginner'
    },
    {
      name: 'Dumbbell Curl',
      target: 'biceps',
      equipment: 'dumbbell',
      instructions: 'Stand with dumbbells at sides. Curl weights toward shoulders. Maintain elbow position throughout movement.',
      difficulty: 'beginner'
    },
    {
      name: 'Hammer Curl',
      target: 'biceps',
      equipment: 'dumbbell',
      instructions: 'Hold dumbbells with neutral grip (hammer position). Curl upward to shoulder height. Lower back down slowly.',
      difficulty: 'beginner'
    },
    {
      name: 'Cable Curl',
      target: 'biceps',
      equipment: 'cable machine',
      instructions: 'Attach straight bar to low pulley. Curl bar to chest height maintaining steady core. Lower back to start.',
      difficulty: 'beginner'
    },
    {
      name: 'Preacher Curl',
      target: 'biceps',
      equipment: 'dumbbell',
      instructions: 'Rest upper arms on preacher bench. Curl weight up in a controlled motion. Lower until arms are nearly straight.',
      difficulty: 'intermediate'
    }
  ],
  triceps: [
    {
      name: 'Tricep Dips',
      target: 'triceps',
      equipment: 'dip bar',
      instructions: 'Grasp parallel bars, support body weight. Lower yourself until elbows are at 90 degrees. Push back up to start.',
      difficulty: 'intermediate'
    },
    {
      name: 'Overhead Tricep Extension',
      target: 'triceps',
      equipment: 'dumbbell',
      instructions: 'Hold dumbbell overhead. Lower weight behind head by bending elbows. Extend back to starting position.',
      difficulty: 'beginner'
    },
    {
      name: 'Tricep Rope Pushdown',
      target: 'triceps',
      equipment: 'cable machine',
      instructions: 'Attach rope to high pulley. Push rope down fully extending arms. Control the return to chest height.',
      difficulty: 'beginner'
    },
    {
      name: 'Bench Dips',
      target: 'triceps',
      equipment: 'bench',
      instructions: 'Hands on bench behind you, feet forward. Lower body until elbows are 90 degrees. Push back up to start.',
      difficulty: 'beginner'
    },
    {
      name: 'Skull Crushers',
      target: 'triceps',
      equipment: 'barbell',
      instructions: 'Lie on bench holding barbell at arms length. Lower bar toward forehead by bending elbows. Extend back up.',
      difficulty: 'intermediate'
    }
  ],
  forearms: [
    {
      name: 'Barbell Wrist Curl',
      target: 'forearms',
      equipment: 'barbell',
      instructions: 'Rest forearms on bench. Curl wrist upward. Lower back to starting position for a full range of motion.',
      difficulty: 'beginner'
    },
    {
      name: 'Reverse Wrist Curl',
      target: 'forearms',
      equipment: 'barbell',
      instructions: 'Rest forearms on bench with palms down. Extend wrists upward. Lower back in controlled motion.',
      difficulty: 'beginner'
    },
    {
      name: 'Farmer Carry',
      target: 'forearms',
      equipment: 'dumbbell',
      instructions: 'Hold heavy dumbbells at your sides. Walk for a set distance maintaining good posture.',
      difficulty: 'beginner'
    },
    {
      name: 'Dumbbell Wrist Curl',
      target: 'forearms',
      equipment: 'dumbbell',
      instructions: 'Rest forearms on bench. Curl dumbbells upward at the wrist. Lower back down slowly.',
      difficulty: 'beginner'
    },
    {
      name: 'Plate Pinch',
      target: 'forearms',
      equipment: 'weight plate',
      instructions: 'Hold two weight plates together. Maintain grip for time. Builds exceptional grip strength.',
      difficulty: 'advanced'
    }
  ],
  legs: [
    {
      name: 'Squats',
      target: 'legs',
      equipment: 'barbell',
      instructions: 'Stand with feet shoulder-width apart. Lower body until thighs are parallel to ground. Push back up to start.',
      difficulty: 'intermediate'
    },
    {
      name: 'Leg Press',
      target: 'legs',
      equipment: 'machine',
      instructions: 'Sit with back against pad. Place feet on platform. Push platform away extending legs fully.',
      difficulty: 'beginner'
    },
    {
      name: 'Walking Lunges',
      target: 'legs',
      equipment: 'body weight',
      instructions: 'Step forward and lower hips until both knees are at 90 degrees. Step forward with opposite leg.',
      difficulty: 'beginner'
    },
    {
      name: 'Leg Curl',
      target: 'legs',
      equipment: 'machine',
      instructions: 'Lie on machine with legs extended. Curl legs toward buttocks. Lower back to start position.',
      difficulty: 'beginner'
    },
    {
      name: 'Deadlift',
      target: 'legs',
      equipment: 'barbell',
      instructions: 'Stand with feet hip-width apart. Deadlift bar from ground to hip height. Lower back down with control.',
      difficulty: 'advanced'
    }
  ],
  abs: [
    {
      name: 'Crunches',
      target: 'abs',
      equipment: 'body weight',
      instructions: 'Lie on back, knees bent. Curl upper body toward knees. Lower back to start position.',
      difficulty: 'beginner'
    },
    {
      name: 'Planks',
      target: 'abs',
      equipment: 'body weight',
      instructions: 'Support body on forearms and toes. Maintain straight line from head to heels. Hold for time.',
      difficulty: 'beginner'
    },
    {
      name: 'Cable Woodchop',
      target: 'abs',
      equipment: 'cable machine',
      instructions: 'Set cable to high position. Rotate body pulling cable diagonally across body. Alternate sides.',
      difficulty: 'intermediate'
    },
    {
      name: 'Hanging Leg Raise',
      target: 'abs',
      equipment: 'pull-up bar',
      instructions: 'Hang from bar with straight arms. Raise legs to horizontal position. Lower back down slowly.',
      difficulty: 'advanced'
    },
    {
      name: 'Ab Wheel Rollout',
      target: 'abs',
      equipment: 'ab wheel',
      instructions: 'Kneel with ab wheel in hands. Roll forward extending body. Roll back to start position.',
      difficulty: 'advanced'
    }
  ],
  cardio: [
    {
      name: 'Running',
      target: 'cardio',
      equipment: 'treadmill',
      instructions: 'Maintain steady pace with proper form. Keep core engaged and arms at 90 degrees.',
      difficulty: 'beginner'
    },
    {
      name: 'Jumping Jacks',
      target: 'cardio',
      equipment: 'body weight',
      instructions: 'Stand with feet together. Jump while spreading feet and raising arms overhead.',
      difficulty: 'beginner'
    },
    {
      name: 'Burpees',
      target: 'cardio',
      equipment: 'body weight',
      instructions: 'Squat down, kick feet back, do push-up, jump feet back, jump up. Complete full cycle.',
      difficulty: 'intermediate'
    },
    {
      name: 'Mountain Climbers',
      target: 'cardio',
      equipment: 'body weight',
      instructions: 'Start in plank. Alternate driving knees toward chest. Maintain steady pace.',
      difficulty: 'beginner'
    },
    {
      name: 'Rowing',
      target: 'cardio',
      equipment: 'rowing machine',
      instructions: 'Sit on rower with feet secured. Drive legs and pull handle to chest. Extend to start position.',
      difficulty: 'intermediate'
    }
  ]
};

const muscleDescriptions = {
  chest: 'Target your chest with pressing and flye movements',
  back: 'Strengthen your back with pulling exercises',
  shoulders: 'Build shoulder strength and stability',
  biceps: 'Isolate and grow your biceps',
  triceps: 'Work your triceps for arm development',
  forearms: 'Develop forearm strength and grip',
  legs: 'Build powerful lower body muscles',
  abs: 'Core strengthening exercises',
  cardio: 'Improve cardiovascular endurance'
};

let currentMuscle = null;

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
  setupMuscleButtons();
});

function setupMuscleButtons() {
  const buttons = document.querySelectorAll('.muscle-btn');
  buttons.forEach(button => {
    button.addEventListener('click', () => {
      const muscle = button.getAttribute('data-muscle');
      selectMuscle(muscle, button);
    });
  });
}

function selectMuscle(muscle, buttonElement) {
  currentMuscle = muscle;
  
  // Update active button
  document.querySelectorAll('.muscle-btn').forEach(btn => {
    btn.classList.remove('active');
  });
  buttonElement.classList.add('active');
  
  // Load and display exercises
  displayExercises(muscle);
}

function displayExercises(muscle) {
  const container = document.getElementById('exercises-container');
  const loading = document.getElementById('loading');
  const noData = document.getElementById('no-data');
  const title = document.getElementById('muscle-title');
  const description = document.getElementById('muscle-description');
  
  // Show loading
  loading.style.display = 'flex';
  container.innerHTML = '';
  noData.style.display = 'none';
  
  // Update header
  title.textContent = muscle.charAt(0).toUpperCase() + muscle.slice(1) + ' Exercises';
  description.textContent = muscleDescriptions[muscle] || 'Targeted exercises for this muscle group';
  
  // Simulate API call with timeout
  setTimeout(() => {
    loading.style.display = 'none';
    
    const exercises = exerciseDatabase[muscle] || [];
    
    if (exercises.length === 0) {
      noData.style.display = 'block';
      return;
    }
    
    // Render exercises
    container.innerHTML = exercises.map(exercise => createExerciseCard(exercise)).join('');
  }, 500);
}

function createExerciseCard(exercise) {
  const difficultyClass = `difficulty-${exercise.difficulty.toLowerCase()}`;
  
  return `
    <div class="exercise-card">
      <h3 class="exercise-name">${exercise.name}</h3>
      
      <div class="exercise-field">
        <span class="exercise-field-label">Equipment</span>
        <div class="exercise-field-value">${capitalizeWords(exercise.equipment)}</div>
      </div>
      
      <div class="exercise-field">
        <span class="exercise-field-label">Instructions</span>
        <div class="exercise-field-value exercise-description">${exercise.instructions}</div>
      </div>
      
      <div class="exercise-field">
        <span class="exercise-field-label">Target</span>
        <div class="exercise-field-value">${capitalizeWords(exercise.target)}</div>
      </div>
      
      <span class="exercise-difficulty ${difficultyClass}">${exercise.difficulty}</span>
    </div>
  `;
}

function capitalizeWords(str) {
  return str
    .split(' ')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
}

// Optional: Function to fetch from real API (e.g., exercisedb.p.rapidapi.com)
async function fetchExercisesFromAPI(muscle) {
  try {
    // This is a template for using a real API
    // Uncomment and add your API key to use
    /*
    const response = await fetch(
      `https://exercisedb.p.rapidapi.com/exercises/target/${muscle}`,
      {
        method: 'GET',
        headers: {
          'X-RapidAPI-Key': 'YOUR_API_KEY_HERE',
          'X-RapidAPI-Host': 'exercisedb.p.rapidapi.com'
        }
      }
    );
    
    if (!response.ok) throw new Error('API fetch failed');
    return await response.json();
    */
    
    // For now, return database exercises
    return exerciseDatabase[muscle] || [];
  } catch (error) {
    console.error('Error fetching exercises:', error);
    return exerciseDatabase[muscle] || [];
  }
}
