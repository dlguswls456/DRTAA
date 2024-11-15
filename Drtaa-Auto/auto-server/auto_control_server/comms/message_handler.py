import json
from utils.logger import setup_logger

logger = setup_logger(__name__)

data_dir = './data'

class MessageHandler:
    def __init__(self):
        # 여기에 차량 관리자 등 필요한 객체를 초기화합니다.
        self.prev_gps = None
        self.prev_orientaion = None

    def handle_message(self, message, source):
        if isinstance(message, dict):
            data = message
        else:
            try:
                data = json.loads(message)
            except json.JSONDecodeError:
                logger.error(f"Invalid JSON received from {source}")
                return {"error": "Invalid JSON"}
        
        action = data.get('action')
        response = self.process_action(action, data)
        logger.info(f"Received from {source}: {action}")
        return response
    
    def process_action(self, action, data):
        if action == 'vehicle_gps':
            return self.handle_vehicle_gps(data)
        elif action == 'vehicle_orientation':
            return self.handle_vehicle_orientaion(data)
        elif action == 'vehicle_global_path':
            return self.handle_vehicle_global_path(data)
        elif action == 'Auto Client Connect':
            return self.handle_connect(data)
        elif action == 'Spring Boot Connect':
            return self.handle_connect(data)
        elif action == 'vehicle_info':
            return self.handle_vehicle_info(data)
        elif action == 'vehicle_dispatch':
            return self.handle_vehicle_dispatch(data)
        elif action == 'vehicle_return':
            return self.handle_vehicle_return(data)
        elif action == 'vehicle_drive':
            return self.handle_vehicle_drive(data)
        elif action == 'vehicle_wait':
            return self.handle_vehicle_wait(data)
        else:
            logger.warning(f"Unknown action received: {action}")
            return {"error": "Unknown action"}

    def handle_connect(self, data):
        return {"status": "success", "echo": data}

    def handle_vehicle_info(self, data):
        # 차량 정보 조회 로직
        return {"status": "success", "info": "Vehicle info placeholder"}

    def handle_vehicle_dispatch(self, data):
        # 차량 호출 로직
        return {"status": "success", "message": "Vehicle dispatched", "latitude": data['latitude'], "longitude": data['longitude']}
    
    def handle_vehicle_return(self, data):
        # 차량 반납 로직
        return {"status": "success", "message": "Vehicle returned"}

    def handle_vehicle_drive(self, data):
        # 차량 주행 로직
        return {"status": "success", "message": "Vehicle driving", "latitude": data['latitude'], "longitude": data['longitude']}
    
    def handle_vehicle_wait(self, data):
        # 차량 대기 로직
        return {"status": "success", "message": "Vehicle waiting"}

    def handle_vehicle_gps(self, data):
        # 차량 GPS
        try:
            with open(f'{data_dir}/gps_data.json') as f:
                self.prev_gps = json.load(f)
                return self.prev_gps
        except FileNotFoundError:
            logger.error(f"gps_data.json 파일을 찾을 수 없습니다.")
            return self.prev_gps
        except json.JSONDecodeError:
            logger.error(f"gps_data.json 파일 형식이 잘못되었습니다.")
            return self.prev_gps
        
    def handle_vehicle_orientaion(self, data):
        # 차량 방향
        try:
            with open(f'{data_dir}/orientation_data.json') as f:
                self.prev_orientaion = json.load(f)
                return self.prev_orientaion
        except FileNotFoundError:
            logger.error(f"orientation_data.json 파일을 찾을 수 없습니다.")
            return self.prev_orientaion
        except json.JSONDecodeError:
            logger.error(f"orientation_data.json 파일 형식이 잘못되었습니다.")
            return self.prev_orientaion
        
    def handle_vehicle_global_path(self, data):
        # 차량 방향
        try:
            with open(f'{data_dir}/global_path.json') as f:
                self.prev_global_path = json.load(f)
                return self.prev_global_path
        except FileNotFoundError:
            logger.error(f"global_path.json 파일을 찾을 수 없습니다.")
            return self.prev_global_path
        except json.JSONDecodeError:
            logger.error(f"global_path.json 파일 형식이 잘못되었습니다.")
            return self.prev_global_path
