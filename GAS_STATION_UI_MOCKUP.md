# Gas Station Screen - UI Mockup with Logout

## Main Screen Layout

```
┌─────────────────────────────────────────────┐
│ 11:25                        ⚡    📶   🔋   │  ← Status bar
├─────────────────────────────────────────────┤
│                                             │
│ Gas Station             [🔴 Logout] [←]    │  ← Header with logout button
│ Fuel Verification                           │
│                                             │
├─────────────────────────────────────────────┤
│                                             │
│ ┌─────────────────────────────────────┐   │
│ │ How to Use                          │   │
│ │                                     │   │
│ │ 1. Tap the QR Scanner button below  │   │
│ │ 2. Scan the transaction QR code     │   │
│ │ 3. Verify transaction details       │   │
│ │ 4. Confirm fuel dispensing          │   │
│ └─────────────────────────────────────┘   │
│                                             │
│ ┌─────────────────────────────────────┐   │
│ │  [QR Code Icon]  Scan QR Code       │   │
│ └─────────────────────────────────────┘   │
│                                             │
│ Pending Transactions                        │
│                                             │
│ ┌─────────────────────────────────────┐   │
│ │                                     │   │
│ │          ✓ (green checkmark)        │   │
│ │                                     │   │
│ │   No pending transactions            │   │
│ │                                     │   │
│ └─────────────────────────────────────┘   │
│                                             │
│                                             │
│                                             │
├─────────────────────────────────────────────┤
│   ≡          ⊙          ←                  │  ← System buttons
└─────────────────────────────────────────────┘
```

## Logout Confirmation Dialog

```
┌─────────────────────────────────────┐
│                                     │
│    [Logout Icon] Logout             │  ← Title (red color)
│         (red icon)                  │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │    [Exit To App Icon - Large]    │ │
│ │         (56x56, red)              │ │
│ │                                   │ │
│ │ Are you sure you want to logout?  │ │
│ └─────────────────────────────────┐ │
│                                     │
│  [Cancel]              [Logout]    │
│  (dark button)          (red btn)   │
│                                     │
└─────────────────────────────────────┘
```

## Color Scheme for Logout Elements

### Logout Button (Header)
```
┌────────────────────┐
│  [Logout Icon]     │  ← Icon: #FF6B6B (Red)
│  Background: Semi-transparent dark blue
│  Corner Radius: 12dp
│  Size: 48x48 dp
└────────────────────┘
```

### Logout Dialog

**Title:**
- Icon: Logout symbol (#FF6B6B)
- Text: "Logout" (#FF6B6B, Bold, 20sp)
- Alignment: Centered

**Content:**
- Icon: Exit icon (#FF6B6B, 56x56 dp)
- Text: "Are you sure you want to logout?" (#E8EFF5, 16sp)
- Background: Dark gradient

**Buttons:**
```
┌──────────────┐  ┌──────────────┐
│   Cancel     │  │   Logout     │
│ (dark, semi) │  │  (red, bold) │
│ Text: White  │  │  Text: White │
└──────────────┘  └──────────────┘
```

## Interaction States

### 1. Normal State
- Logout button visible in top-right
- Red color (#FF6B6B) indicates logout action
- Icon: `Icons.Default.Logout`
- Easily accessible without obstructing QR scanner

### 2. Hover/Pressed State
- Button slightly brightens or shows ripple effect
- Dialog appears with smooth fade-in animation
- Background dims (30-40% opacity)

### 3. Dialog Open
- Dialog centered on screen
- Modal overlay prevents interaction with background
- Focus on logout confirmation

### 4. Dialog Actions
```
User clicks "Cancel"
    ↓
Dialog closes
    ↓
Returns to QR scanner screen

User clicks "Logout"
    ↓
Dialog closes
    ↓
Triggers logout callback
    ↓
AuthViewModel.logout() called
    ↓
User session cleared
    ↓
Navigate to Login screen
```

## Responsive Design

### Portrait (Primary)
- Full screen layout optimized
- Buttons at top corners
- Dialog centered

### Landscape (Secondary)
- Header adjusts for wider screen
- Dialog remains centered
- Buttons maintain accessibility

## Accessibility Features

✅ **Touch Targets**: All buttons are 48x48 dp (minimum recommended)
✅ **Color Contrast**: Red (#FF6B6B) contrasts with dark background
✅ **Icons**: Descriptive labels for all icons
✅ **Dialog**: Clear confirmation message
✅ **State**: Visual feedback on button clicks

## Animation Details

### Dialog Appearance
```
Fade in: 200-300ms
Scale: From 0.8 to 1.0
Movement: Slight upward slide
```

### Button States
```
Normal → Hover: Subtle brightness increase
Hover → Pressed: Background color shift
Release: Return to normal with ripple effect
```

## Accessibility Considerations

- Logout button is red to indicate a destructive action
- Dialog requires explicit confirmation
- Cancel button allows backing out
- Clear, simple language in dialog
- Icons support text labels
- Touch targets are large enough (48x48 minimum)

## Implementation Notes

- Uses Material 3 AlertDialog
- Composable design for reusability
- Theme colors from the app's color system
- Responsive padding and sizing
- No external assets required (uses Material icons)
