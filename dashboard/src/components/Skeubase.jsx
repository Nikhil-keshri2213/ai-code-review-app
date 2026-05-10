// ─── Skeuomorphic Design Tokens & Primitives ───────────────────────────────
export const C = {
  pageBg:      '#141414',
  panelGrad:   'linear-gradient(145deg, #272727 0%, #1d1d1d 60%, #222 100%)',
  insetBg:     '#0b0b0b',
  border:      '1px solid #323232',
  raisedShadow:'0 8px 28px rgba(0,0,0,0.85), inset 0 1px 0 rgba(255,255,255,0.07), inset 0 -1px 0 rgba(0,0,0,0.5)',
  insetShadow: 'inset 0 4px 14px rgba(0,0,0,0.95), inset 0 1px 4px rgba(0,0,0,0.7)',
  gold:        '#c8900a',
  goldBright:  '#e8c060',
  textPrimary: '#ddd6c6',
  textSecondary:'#6a6258',
  textMuted:   '#3d3830',
  mono:        "'Share Tech Mono', 'Courier New', 'Lucida Console', monospace",
}

// Decorative corner screw
export const Screw = ({ style = {} }) => (
  <div style={{
    width: 9, height: 9, borderRadius: '50%',
    background: 'radial-gradient(circle at 38% 32%, #484440, #1a1816)',
    boxShadow: '0 1px 3px rgba(0,0,0,0.9), inset 0 1px 0 rgba(255,255,255,0.07)',
    position: 'absolute',
    ...style,
  }}>
    {/* Phillips-head slot */}
    <div style={{ position:'absolute', top:'50%', left:'15%', right:'15%', height:1, background:'rgba(0,0,0,0.6)', transform:'translateY(-50%)' }} />
    <div style={{ position:'absolute', left:'50%', top:'15%', bottom:'15%', width:1, background:'rgba(0,0,0,0.6)', transform:'translateX(-50%)' }} />
  </div>
)

// Raised metal panel wrapper
export const Panel = ({ children, style = {}, screws = false }) => (
  <div style={{
    background: C.panelGrad,
    borderRadius: 14,
    border: C.border,
    boxShadow: C.raisedShadow,
    position: 'relative',
    ...style,
  }}>
    {screws && (
      <>
        <Screw style={{ top: 10, left: 10 }} />
        <Screw style={{ top: 10, right: 10 }} />
        <Screw style={{ bottom: 10, left: 10 }} />
        <Screw style={{ bottom: 10, right: 10 }} />
      </>
    )}
    {children}
  </div>
)

// Inset / recessed surface (LCD screen, table area)
export const Inset = ({ children, style = {} }) => (
  <div style={{
    background: C.insetBg,
    boxShadow: C.insetShadow,
    border: '1px solid #080808',
    borderRadius: 10,
    overflow: 'hidden',
    ...style,
  }}>
    {children}
  </div>
)

// Gold top accent stripe
export const GoldLine = () => (
  <div style={{
    height: 2,
    background: `linear-gradient(90deg, transparent, ${C.gold}, ${C.goldBright}, ${C.gold}, transparent)`,
    borderRadius: '14px 14px 0 0',
  }} />
)

// LED indicator dot
export const LED = ({ color, glow, size = 7 }) => (
  <div style={{
    width: size, height: size, borderRadius: '50%',
    background: color,
    boxShadow: `0 0 6px 2px ${glow}`,
    flexShrink: 0,
  }} />
)

// Section label row with right-fade rule
export const SectionLabel = ({ children, right = null }) => (
  <div style={{ display:'flex', alignItems:'center', gap:10, marginBottom:16 }}>
    <span style={{ fontFamily: C.mono, fontSize:10, letterSpacing:'0.14em', color: C.textSecondary, whiteSpace:'nowrap' }}>
      {children}
    </span>
    <div style={{ flex:1, height:1, background:'linear-gradient(90deg, #2e2e2e, transparent)' }} />
    {right && <span style={{ fontFamily: C.mono, fontSize:9, color: C.textMuted }}>{right}</span>}
  </div>
)