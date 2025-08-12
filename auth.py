from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow
from google.auth.transport.requests import Request
from googleapiclient.discovery import build
from config import Config

SCOPES = ["https://www.googleapis.com/auth/spreadsheets.readonly"]

def get_sheets_service():
    config = Config()

    credentials = None
    if config.TOKEN_PATH.exists():
        credentials = Credentials.from_authorized_user_file(config.TOKEN_PATH, SCOPES)
    if not credentials or not credentials.valid:
        if credentials and credentials.expired and credentials.refresh_token:
            credentials.refresh(Request())
        else:
            if not config.CREDENTIALS_PATH.exists():
                raise FileNotFoundError(f"{config.CREDENTIALS_PATH} not found.")
            flow = InstalledAppFlow.from_client_secrets_file(
                str(config.CREDENTIALS_PATH), SCOPES
            )
            credentials = flow.run_local_server(port=0)
        with open(config.TOKEN_PATH, "w") as token_file:
            token_file.write(credentials.to_json())
    return build("sheets", "v4", credentials=credentials)
