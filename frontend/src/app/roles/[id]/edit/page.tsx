"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { useRouter } from "next/navigation"
import { RefreshCw } from "lucide-react"
import Link from "next/link"
import { fetchRoleById, updateRole } from "@/app/api/roles/api"
import { toast } from "@/components/ui/use-toast"
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from "@/components/ui/card";
import { Alert, AlertDescription } from "@/components/ui/alert";


interface Role {
  id: number
  name: string
}

export default function EditRolePage({ params }: { params: { id: string } }) {
  const router = useRouter()

  const [role, setRole] = useState<Role | null>(null)
  const [roleName, setRoleName] = useState("")
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [isCreate, setIsCreate] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // If the ID is "create", redirect to the create page
  useEffect(() => {
    if (params.id === "create") {
      router.replace("/roles/create")
      setIsCreate(true)
    } else {
      setIsCreate(false)
    }
  }, [params.id, router])

  // If the ID is "create", show a loading state until the redirect happens
  if (isCreate) {
    return (
      <div className="container mx-auto py-12 flex justify-center">
        <RefreshCw className="h-8 w-8 animate-spin" />
      </div>
    )
  }

  useEffect(() => {
    const fetchRoleData = async () => {
      setLoading(true)
      try {
        // Check if the ID is a valid number
        const roleId = Number.parseInt(params.id)
        if (isNaN(roleId)) {
          throw new Error("Invalid role ID")
        }

        const data = await fetchRoleById(roleId)
        setRole(data)
        setRoleName(data.name)
      } catch (error) {
        setError(error instanceof Error ? error.message : "Failed to load role")
      } finally {
        setLoading(false)
      }
    }

    fetchRoleData()
  }, [params.id])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true)

    try {
      const roleId = Number.parseInt(params.id)
      await updateRole(roleId, { id: roleId, name: roleName })
      
      router.push("/roles")
    } catch (error) {
      setError(error instanceof Error ? error.message : "Failed to update role")
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="container mx-auto py-12 flex justify-center">
        <RefreshCw className="h-8 w-8 animate-spin" />
      </div>
    )
  }

  if (!role) {
    return (
      <div className="container mx-auto py-12">
        <Button asChild className="mt-4">
          <Link href="/roles">Back to List</Link>
        </Button>
      </div>
    )
  }

  return (
    <div className="container mx-auto py-6 max-w-2xl">
      <Card>
        <CardHeader>
          <CardTitle>Edit Role</CardTitle>
        </CardHeader>

        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="space-y-2">
              <label className="flex items-center">
                <span className="font-medium">Role Name</span>
              </label>
              <Input
                type="text"
                value={roleName}
                onChange={(e) => setRoleName(e.target.value)}
                required
              />
            </div>

            {error && (
              <Alert variant="default">
                <AlertDescription>{error}</AlertDescription>
              </Alert>
            )}

            <div className="flex justify-end gap-2">
              <Button variant="outline" type="submit" disabled={saving}>
                {saving ? "Saving..." : "Save"}
              </Button>
              <Button variant="default" asChild>
                <Link href="/roles">Back to List</Link>
              </Button>
            </div>
          </form>
        </CardContent>

        <CardFooter className="bg-gray-50 rounded-b-lg border-t">
          <div className="flex items-center text-amber-600">
            <div className="mr-2 text-2xl">💡</div>
            <p className="text-sm">
              Roles define access levels within the system. Make sure to use descriptive names for roles.
            </p>
          </div>
        </CardFooter>
      </Card>
    </div>
  );
}
