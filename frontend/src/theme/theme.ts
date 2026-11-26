import {createTheme} from '@mui/material/styles'

export const theme = createTheme({
    palette: {
        mode: 'light',
        primary: {
            main: '#1565C0',
        },
        secondary: {
            main: '#00897B',
        },
        background: {
            default: '#F5F7FA',
            paper: '#FFFFFF',
        },
    },
    shape: {
        borderRadius: 10,
    },
    typography: {
        fontFamily: 'Roboto, Arial, sans-serif',
    }
})