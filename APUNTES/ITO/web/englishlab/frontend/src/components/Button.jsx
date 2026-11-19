/**
 * Botón del Figma: .btn + variantes .alt/.rust/.olive/.small/.wide.
 * (El markup sigue usando esas clases exactas; este componente evita repetición.)
 */
export default function Button({
  variant = '', // '' | 'alt' | 'rust' | 'olive'
  size = '',    // '' | 'small'
  wide = false,
  className = '',
  children,
  ...rest
}) {
  const cls = ['btn'];
  if (variant) cls.push(variant);
  if (size) cls.push(size);
  if (wide) cls.push('wide');
  if (className) cls.push(className);
  return (
    <button type="button" className={cls.join(' ')} {...rest}>
      {children}
    </button>
  );
}
