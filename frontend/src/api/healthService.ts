
import { httpClient } from './httpClient';

type HealthResponse = {
  status: string;
};

export async function getApiHealth(): Promise<HealthResponse> {
  const response = await httpClient.get<HealthResponse>(
    '/actuator/health'
  );

  return response.data;
}
