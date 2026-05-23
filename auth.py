from pathlib import Path
import logging

from google.auth.exceptions import RefreshError
from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow
from google.auth.transport.requests import Request
from googleapiclient.discovery import build
from typing import cast

from config import Config

SCOPES = ["https://www.googleapis.com/auth/spreadsheets.readonly"]

logger = logging.getLogger(__name__)

def _run_oauth_flow(config: Config) -> Credentials:
    if not config.CREDENTIALS_PATH.exists():
        raise FileNotFoundError(f"{config.CREDENTIALS_PATH} not found.")

    flow = InstalledAppFlow.from_client_secrets_file(
        str(config.CREDENTIALS_PATH),
        SCOPES,
    )
    
    credentials = flow.run_local_server(port=0)
    return cast(Credentials, credentials)

def _save_token(credentials: Credentials, token_path: Path) -> None:
    token_path.parent.mkdir(parents=True, exist_ok=True)

    with open(token_path, "w", encoding="utf-8") as token_file:
        token_file.write(credentials.to_json())

def get_sheets_service():
    config = Config()

    credentials = None
    
    if config.TOKEN_PATH.exists():
        credentials = Credentials.from_authorized_user_file(
            config.TOKEN_PATH,
            SCOPES,
            )

    if not credentials or not credentials.valid:
        if credentials and credentials.expired and credentials.refresh_token:
            try:
                credentials.refresh(Request())
            except RefreshError:
                logger.warning("Failed to refresh token, running OAuth flow again")
                credentials = _run_oauth_flow(config)
        
        else:
            credentials = _run_oauth_flow(config)
            
        _save_token(credentials, config.TOKEN_PATH)
        
    return build("sheets", "v4", credentials=credentials)
