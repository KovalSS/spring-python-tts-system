export const VOICE_OPTIONS = [
  { value: '', label: 'За замовчуванням' },
  { value: 'uk-UA-OstapNeural', label: 'Український чоловічий голос — Остап' },
  { value: 'uk-UA-PolinaNeural', label: 'Український жіночий голос — Поліна' },
  { value: 'en-US-JennyNeural', label: 'Англійський (US) — Дженні' },
];

export const RATE_OPTIONS = [
  { value: '', label: 'За замовчуванням' },
  { value: '-50%', label: 'Повільно' },
  { value: '-25%', label: 'Трохи повільніше' },
  { value: '+0%', label: 'Нормально' },
  { value: '+25%', label: 'Трохи швидше' },
  { value: '+50%', label: 'Швидко' },
];

export const PITCH_OPTIONS = [
  { value: '', label: 'За замовчуванням' },
  { value: '-10Hz', label: 'Нижчий' },
  { value: '-5Hz', label: 'Трохи нижчий' },
  { value: '+0Hz', label: 'Нормально' },
  { value: '+5Hz', label: 'Трохи вищий' },
  { value: '+10Hz', label: 'Вищий' },
];

export const VOLUME_OPTIONS = [
  { value: '', label: 'За замовчуванням' },
  { value: '-20%', label: 'Тихіше' },
  { value: '-10%', label: 'Трохи тихіше' },
  { value: '+0%', label: 'Нормально' },
  { value: '+10%', label: 'Трохи голосніше' },
  { value: '+20%', label: 'Голосніше' },
];

function labelFor(options, value) {
  return options.find((option) => option.value === value)?.label || 'За замовчуванням';
}

export function formatVoice(value) {
  return labelFor(VOICE_OPTIONS, value);
}

export function formatRate(value) {
  return labelFor(RATE_OPTIONS, value);
}

export function formatPitch(value) {
  return labelFor(PITCH_OPTIONS, value);
}

export function formatVolume(value) {
  return labelFor(VOLUME_OPTIONS, value);
}

