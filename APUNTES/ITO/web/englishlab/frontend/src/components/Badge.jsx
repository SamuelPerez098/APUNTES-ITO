/** Badge del Figma: .badge + .green/.red/.gray/.gold. */
export default function Badge({ color = 'gray', children }) {
  return <span className={`badge ${color}`}>{children}</span>;
}
