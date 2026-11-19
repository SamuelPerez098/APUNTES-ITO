/**
 * Input con etiqueta, ayuda y MENSAJE DE ERROR DEBAJO DEL CAMPO (react-ui-rules).
 * El error se muestra mientras se escribe y en blur.
 */
export default function Input({
  label,
  name,
  type = 'text',
  placeholder,
  help,
  error,
  autoComplete,
  value,
  onChange,
  onBlur,
  className = '',
}) {
  const invalid = Boolean(error);
  return (
    <div className={className}>
      <label className="form-label" htmlFor={name}>
        {label}
      </label>
      <input
        id={name}
        name={name}
        type={type}
        className={`input${invalid ? ' invalid' : ''}`}
        placeholder={placeholder}
        autoComplete={autoComplete}
        value={value}
        onChange={onChange}
        onBlur={onBlur}
        aria-invalid={invalid}
        aria-describedby={invalid ? `${name}-error` : undefined}
      />
      {error ? (
        <p className="error" id={`${name}-error`} role="alert">
          {error}
        </p>
      ) : help ? (
        <p className="help">{help}</p>
      ) : null}
    </div>
  );
}
